package net.dankito.banking.ui.presenter

import net.dankito.banking.persistence.IBankingPersistence
import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.IBankingClient
import net.dankito.banking.ui.IBankingClientCreator
import net.dankito.banking.ui.IRouter
import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.model.responses.GetTransactionsResponse
import net.dankito.banking.bankfinder.IBankFinder
import net.dankito.banking.bankfinder.BankInfo
import net.dankito.banking.search.IRemitteeSearcher
import net.dankito.banking.search.NoOpRemitteeSearcher
import net.dankito.banking.search.Remittee
import net.dankito.banking.ui.model.mapper.DefaultModelCreator
import net.dankito.banking.ui.model.mapper.IModelCreator
import net.dankito.banking.ui.model.moneytransfer.ExtractTransferMoneyDataFromPdfResult
import net.dankito.banking.ui.model.moneytransfer.ExtractTransferMoneyDataFromPdfResultType
import net.dankito.banking.ui.model.parameters.GetTransactionsParameter
import net.dankito.banking.ui.model.settings.AppSettings
import net.dankito.banking.ui.model.tan.*
import net.dankito.banking.util.*
import net.dankito.banking.util.extraction.IInvoiceDataExtractor
import net.dankito.banking.util.extraction.ITextExtractorRegistry
import net.dankito.banking.util.extraction.NoOpInvoiceDataExtractor
import net.dankito.banking.util.extraction.NoOpTextExtractorRegistry
import net.dankito.utils.multiplatform.*
import net.dankito.utils.multiplatform.log.LoggerFactory
import kotlin.collections.ArrayList


open class BankingPresenter(
    protected val bankingClientCreator: IBankingClientCreator,
    protected val bankFinder: IBankFinder,
    protected val dataFolder: File,
    protected val persister: IBankingPersistence,
    protected val router: IRouter,
    protected val modelCreator: IModelCreator = DefaultModelCreator(),
    protected val remitteeSearcher: IRemitteeSearcher = NoOpRemitteeSearcher(),
    protected val bankIconFinder: IBankIconFinder = NoOpBankIconFinder(),
    protected val textExtractorRegistry: ITextExtractorRegistry = NoOpTextExtractorRegistry(),
    protected val invoiceDataExtractor: IInvoiceDataExtractor = NoOpInvoiceDataExtractor(),
    protected val serializer: ISerializer = NoOpSerializer(),
    protected val asyncRunner: IAsyncRunner = CoroutinesAsyncRunner()
) {

    companion object {
        val ChipTanTanProcedures = listOf(TanProcedureType.ChipTanManuell, TanProcedureType.ChipTanFlickercode, TanProcedureType.ChipTanUsb,
                                            TanProcedureType.ChipTanQrCode, TanProcedureType.ChipTanPhotoTanMatrixCode)

        protected const val OneDayMillis = 24 * 60 * 60 * 1000

        protected val MessageLogEntryDateFormatter = DateFormatter("yyyy.MM.dd HH:mm:ss.SSS")

        private val log = LoggerFactory.getLogger(BankingPresenter::class)
    }


    protected val bankingClientsForAccounts = mutableMapOf<TypedCustomer, IBankingClient>()

    protected var selectedBankAccountsField = mutableListOf<TypedBankAccount>()

    protected var selectedAccountType = SelectedAccountType.AllAccounts

    protected var saveAccountOnNextEnterTanInvocation = false


    protected val accountsChangedListeners = mutableListOf<(List<TypedCustomer>) -> Unit>()

    protected val retrievedAccountTransactionsResponseListeners = mutableListOf<(GetTransactionsResponse) -> Unit>()

    protected val selectedBankAccountsChangedListeners = mutableListOf<(List<TypedBankAccount>) -> Unit>()


    protected val callback: BankingClientCallback = object : BankingClientCallback {

        override fun enterTan(customer: TypedCustomer, tanChallenge: TanChallenge, callback: (EnterTanResult) -> Unit) {
            if (saveAccountOnNextEnterTanInvocation) {
                persistAccountOffUiThread(customer)
                saveAccountOnNextEnterTanInvocation = false
            }

            router.getTanFromUserFromNonUiThread(customer, tanChallenge, this@BankingPresenter) { result ->
                if (result.changeTanProcedureTo != null || result.changeTanMediumTo != null) { // then either selected TAN medium or procedure will change -> save account on next call to enterTan() as then changes will be visible
                    saveAccountOnNextEnterTanInvocation = true
                }

                callback(result)
            }
        }

        override fun enterTanGeneratorAtc(tanMedium: TanGeneratorTanMedium, callback: (EnterTanGeneratorAtcResult) -> Unit) {
            router.getAtcFromUserFromNonUiThread(tanMedium) { result ->
                callback(result)
            }
        }

    }


    init {
        asyncRunner.runAsync {
            readAppSettings()
            readPersistedAccounts()

            updateAccountsTransactionsIfNoTanIsRequiredAsync()
        }

        // preloadBankList asynchronously; on Android it takes approximately 18 seconds till banks are indexed for first time -> do it as early as possible
        asyncRunner.runAsync {
            bankFinder.preloadBankList()
        }
    }


    protected open fun readPersistedAccounts() {
        try {
            val deserializedAccounts = persister.readPersistedAccounts()

            deserializedAccounts.forEach { customer ->
                val newClient = bankingClientCreator.createClient(customer, dataFolder, asyncRunner, callback)

                addClientForAccount(customer, newClient)
            }

            callAccountsChangedListeners()

            selectedAllBankAccounts() // TODO: save last selected bank account(s)
        } catch (e: Exception) {
            log.error(e) { "Could not deserialize persisted accounts with persister $persister" }
        }
    }

    protected open fun addClientForAccount(customer: TypedCustomer, client: IBankingClient) {
        bankingClientsForAccounts.put(customer, client)
    }


    // TODO: move BankInfo out of fints4k
    open fun addAccountAsync(bankInfo: BankInfo, customerId: String, password: String, callback: (AddAccountResponse) -> Unit) {
        val customer = modelCreator.createCustomer(bankInfo.bankCode, customerId, password, bankInfo.pinTanAddress ?: "", bankInfo.name, bankInfo.bic, "")

        val newClient = bankingClientCreator.createClient(customer, dataFolder, asyncRunner, this.callback)

        val startDate = Date()

        newClient.addAccountAsync { response ->
            val account = response.customer
            account.displayIndex = customers.size

            if (response.isSuccessful) {
                addClientForAccount(account, newClient)

                selectedAccount(account)

                callAccountsChangedListeners()

                persistAccountOffUiThread(account)

                response.retrievedData.forEach { retrievedData ->
                    retrievedAccountTransactions(GetTransactionsResponse(retrievedData.account, true, null,
                        listOf(retrievedData)), startDate, false
                    )
                }

                findIconForBankAsync(account)
            }

            callback(response)
        }
    }

    protected open fun findIconForBankAsync(customer: TypedCustomer) {
        bankIconFinder.findIconForBankAsync(customer.bankName) { bankIconUrl ->
            bankIconUrl?.let {
                try {
                    handleFindIconForBankResult(customer, bankIconUrl)
                } catch (e: Exception) {
                    log.error(e) { "Could not get icon for bank ${customer.bankName}" }
                }
            }
        }
    }

    protected open fun handleFindIconForBankResult(customer: TypedCustomer, bankIconUrl: String) {
        val bankIconFile = saveBankIconToDisk(customer, bankIconUrl)

        var iconFilePath = bankIconFile.getAbsolutePath()

        if (iconFilePath.startsWith("file://", true) == false) {
            iconFilePath = "file://" + iconFilePath // without 'file://' Android will not find it
        }

        customer.iconUrl = iconFilePath

        persistAccountOffUiThread(customer)

        callAccountsChangedListeners()
    }

    protected open fun saveBankIconToDisk(customer: TypedCustomer, bankIconUrl: String): File {
        val bankIconsDir = File(dataFolder, "bank_icons")
        bankIconsDir.mkdirs()

        val extension = getIconFileExtension(bankIconUrl)
        val bankIconFile = File(bankIconsDir, customer.bankCode + if (extension != null) (".$extension") else "")

        persister.saveUrlToFile(bankIconUrl, bankIconFile)

        return bankIconFile
    }

    protected open fun getIconFileExtension(bankIconUrl: String): String? {
        try {
            var iconFilename = File(bankIconUrl).filename
            if (iconFilename.contains('?')) {
                iconFilename = iconFilename.substring(0, iconFilename.indexOf('?'))
            }

            val extension = File(iconFilename).fileExtension
            if (extension.isNotBlank()) {
                return extension
            }
        } catch (e: Exception) {
            log.info(e) { "Could not get icon file extension from url '$bankIconUrl'" }
        }

        return null
    }


    open fun deleteAccount(customer: TypedCustomer) {
        asyncRunner.runAsync {
            deleteAccountOffUiThread(customer)
        }
    }

    protected open fun deleteAccountOffUiThread(customer: TypedCustomer) {
        val wasSelected = isSingleSelectedAccount(customer) or // either account or one of its bank accounts is currently selected
                (customer.accounts.firstOrNull { isSingleSelectedBankAccount(it) } != null)

        val client = bankingClientsForAccounts.remove(customer)

        val displayIndex = customer.displayIndex

        persister.deleteAccount(customer, customers)

        val sortedBanks = customers.sortedByDisplayIndex()
        for (i in IntRange(displayIndex, sortedBanks.size - 1)) {
            val bank = sortedBanks[i]
            bank.displayIndex = i
            accountDisplayIndexUpdated(bank)
        }

        client?.deletedAccount(customer, customers.firstOrNull { it.customerId == customer.customerId && it.bankCode == customer.bankCode} == null)

        callAccountsChangedListeners()

        if (wasSelected || areAllAccountSelected) { // to update displayed account transactions as transactions of yet deleted accounts have to be removed
            selectedAllBankAccounts()
        }
    }


    open fun fetchAllAccountTransactionsAsync(customer: TypedCustomer,
                                              callback: ((GetTransactionsResponse) -> Unit)? = null) {

        customer.accounts.forEach { bankAccount ->
            if (bankAccount.supportsRetrievingAccountTransactions) {
                fetchAllAccountTransactionsAsync(bankAccount, callback) // TODO: use a synchronous version of fetchAccountTransactions() so that all bank accounts get handled serially
            }
        }
    }

    open fun fetchAllAccountTransactionsAsync(bankAccount: TypedBankAccount,
                                              callback: ((GetTransactionsResponse) -> Unit)? = null) {

        fetchAccountTransactionsAsync(bankAccount, null, false, callback)
    }

    open fun fetchAccountTransactionsAsync(bankAccount: TypedBankAccount, fromDate: Date?, abortIfTanIsRequired: Boolean = false,
                                           callback: ((GetTransactionsResponse) -> Unit)? = null) {

        getBankingClientForAccount(bankAccount.customer)?.let { client ->
            val startDate = Date()

            client.getTransactionsAsync(bankAccount, GetTransactionsParameter(true, fromDate, null, abortIfTanIsRequired, { receivedAccountsTransactionChunk(bankAccount, it) } )) { response ->

                if (response.tanRequiredButWeWereToldToAbortIfSo == false) { // don't call retrievedAccountTransactions() if aborted due to TAN required but we told client to abort if so
                    retrievedAccountTransactions(response, startDate, fromDate == null)
                }

                callback?.invoke(response)
            }
        }
    }

    open fun updateAccountsTransactionsAsync(callback: (GetTransactionsResponse) -> Unit) {
        updateAccountsTransactionsAsync(false, callback)
    }

    open fun updateAccountsTransactionsIfNoTanIsRequiredAsync() {
        updateAccountsTransactionsAsync(true) { }
    }

    open fun updateSelectedBankAccountTransactionsAsync(callback: (GetTransactionsResponse) -> Unit) {
        updateBanksAccountsTransactionsAsync(selectedBankAccounts, false, callback)
    }

    protected open fun updateAccountsTransactionsAsync(abortIfTanIsRequired: Boolean = false, callback: (GetTransactionsResponse) -> Unit) {
        bankingClientsForAccounts.keys.forEach { account ->
            account.accounts.forEach { bankAccount ->
                if (bankAccount.supportsRetrievingAccountTransactions) {
                    updateBankAccountTransactionsAsync(bankAccount, abortIfTanIsRequired, callback)
                }
            }
        }
    }

    protected open fun updateBanksAccountsTransactionsAsync(accounts: List<TypedBankAccount>, abortIfTanIsRequired: Boolean = false, callback: (GetTransactionsResponse) -> Unit) {
        accounts.forEach { bankAccount ->
            if (bankAccount.supportsRetrievingAccountTransactions) {
                updateBankAccountTransactionsAsync(bankAccount, abortIfTanIsRequired, callback)
            }
        }
    }

    protected open fun updateBankAccountTransactionsAsync(bankAccount: TypedBankAccount, abortIfTanIsRequired: Boolean, callback: (GetTransactionsResponse) -> Unit) {
        val fromDate = bankAccount.lastRetrievedTransactionsTimestamp?.let { Date(it.millisSinceEpoch - OneDayMillis) } // one day before last received transactions

        fetchAccountTransactionsAsync(bankAccount, fromDate, abortIfTanIsRequired, callback)
    }

    protected open fun retrievedAccountTransactions(response: GetTransactionsResponse, startDate: Date, didFetchAllTransactions: Boolean) {
        if (response.isSuccessful) {
            response.retrievedData.forEach { retrievedData ->
                retrievedData.account.lastRetrievedTransactionsTimestamp = startDate

                if (didFetchAllTransactions) {
                    retrievedData.account.haveAllTransactionsBeenFetched = true
                }

                updateAccountTransactionsAndBalances(retrievedData)
            }
        }

        callRetrievedAccountTransactionsResponseListener(response)
    }

    protected open fun receivedAccountsTransactionChunk(bankAccount: TypedBankAccount, accountTransactionsChunk: List<IAccountTransaction>) {
        if (accountTransactionsChunk.isNotEmpty()) {
            asyncRunner.runAsync { // don't block retrieving next chunk by blocked saving to db / json
                updateAccountTransactions(bankAccount, accountTransactionsChunk)

                callRetrievedAccountTransactionsResponseListener(GetTransactionsResponse(bankAccount, true, null, listOf(RetrievedAccountData(bankAccount, null, accountTransactionsChunk, listOf()))))
            }
        }
    }

    protected open fun updateAccountTransactionsAndBalances(retrievedData: RetrievedAccountData) {
        updateAccountTransactions(retrievedData.account, retrievedData.bookedTransactions, retrievedData.unbookedTransactions)

        retrievedData.balance?.let {
            updateBalance(retrievedData.account, it)
        }
    }

    protected open fun updateAccountTransactions(bankAccount: TypedBankAccount, bookedTransactions: Collection<IAccountTransaction>, unbookedTransactions: List<Any>? = null) {
        val knownAccountTransactions = bankAccount.bookedTransactions.map { it.transactionIdentifier }

        val newBookedTransactions = bookedTransactions.filterNot { knownAccountTransactions.contains(it.transactionIdentifier) }
        bankAccount.addBookedTransactions(newBookedTransactions)

        unbookedTransactions?.let {
            bankAccount.addUnbookedTransactions(unbookedTransactions)
        }

        persistAccountTransactionsOffUiThread(bankAccount, newBookedTransactions)
    }

    protected open fun updateBalance(bankAccount: TypedBankAccount, balance: BigDecimal) {
        bankAccount.balance = balance

        persistAccountOffUiThread(bankAccount.customer)
    }


    open fun formatAmount(amount: BigDecimal): String {
        return amount.format(2)
    }


    open fun allAccountsUpdated() {
        customers.forEach { account ->
            accountDisplayIndexUpdated(account)
        }
    }

    open fun accountDisplayIndexUpdated(account: TypedCustomer) {
        persistAccountAsync(account)

        callAccountsChangedListeners()
    }

    open fun accountUpdated(bank: TypedCustomer) {
        persistAccountAsync(bank)

        callAccountsChangedListeners()

        getBankingClientForAccount(bank)?.dataChanged(bank)
    }

    open fun accountUpdated(account: TypedBankAccount) {
        persistAccountAsync(account.customer)

        callAccountsChangedListeners()
    }

    protected open fun persistAccountAsync(customer: ICustomer<*, *>) {
        asyncRunner.runAsync {
            persistAccountOffUiThread(customer)
        }
    }

    /**
     * Ensure that this method only gets called off UI thread (at least for Android Room db) as otherwise it may blocks UI thread.
     */
    protected open fun persistAccountOffUiThread(customer: ICustomer<*, *>) {
        persister.saveOrUpdateAccount(customer as TypedCustomer, customers)
    }

    /**
     * Ensure that this method only gets called off UI thread (at least for Android Room db) as otherwise it may blocks UI thread.
     */
    protected open fun persistAccountTransactionsOffUiThread(bankAccount: TypedBankAccount, bookedTransactions: List<IAccountTransaction>) {
        persister.saveOrUpdateAccountTransactions(bankAccount, bookedTransactions)
    }


    open fun transferMoneyAsync(data: TransferMoneyData, callback: (BankingClientResponse) -> Unit) {
        val account = data.account

        getBankingClientForAccount(account.customer)?.let { client ->
            client.transferMoneyAsync(data) { response ->
                if (response.isSuccessful) {
                    updateBankAccountTransactionsAsync(account, true) { }
                }

                callback(response)
            }
        }
    }

    open fun transferMoneyWithDataFromPdf(pdf: File): ExtractTransferMoneyDataFromPdfResult {
        val extractionResult = textExtractorRegistry.extractTextWithBestExtractorForFile(pdf)

        if (extractionResult.couldExtractText == false || extractionResult.text == null) {
            val resultType = if (extractionResult.noExtractorFound) ExtractTransferMoneyDataFromPdfResultType.NotASearchablePdf
                        else ExtractTransferMoneyDataFromPdfResultType.CouldNotExtractText
            return ExtractTransferMoneyDataFromPdfResult(resultType, extractionResult.exception)
        }
        else {
            extractionResult.text?.let { extractedText ->
                val invoiceData = invoiceDataExtractor.extractInvoiceData(extractedText)

                if (invoiceData.potentialTotalAmount != null || invoiceData.potentialIban != null) { // if at least an amount or IBAN could get extracted
                    val transferMoneyData = TransferMoneyData(
                        bankAccounts.first(), "",
                        invoiceData.potentialIban ?: "",
                        invoiceData.potentialBic ?: "",
                        invoiceData.potentialTotalAmount ?: BigDecimal.Zero, "")
                    showTransferMoneyDialog(transferMoneyData)
                }
                else {
                    return ExtractTransferMoneyDataFromPdfResult(
                        ExtractTransferMoneyDataFromPdfResultType.CouldNotExtractInvoiceDataFromExtractedText, invoiceData.error)
                }
            }
        }

        return ExtractTransferMoneyDataFromPdfResult(ExtractTransferMoneyDataFromPdfResultType.Success)
    }


    open fun findUniqueBankForIbanAsync(iban: String, callback: (BankInfo?) -> Unit) {
        asyncRunner.runAsync {
            callback(findUniqueBankForIban(iban))
        }
    }

    open fun findUniqueBankForIban(iban: String): BankInfo? {
        val ibanWithoutWhiteSpaces = iban.replace(" ", "")

        // first two characters are country code, 3rd and 4th character are checksum, bank code has 8 digits in Germany and user
        // should enter at least five characters before we start searching (before there shouldn't be a chance of a unique result)
        if (ibanWithoutWhiteSpaces.length >= 9) {
            if (ibanWithoutWhiteSpaces.startsWith("DE")) {
                return findUniqueBankForBankCode(ibanWithoutWhiteSpaces.substring(4).ofMaxLength(8))
            }
        }

        return null
    }

    open fun findUniqueBankForBankCode(bankCode: String): BankInfo? {
        val searchResult = bankFinder.findBankByBankCode(bankCode)

        val groupedByBic = searchResult.groupBy { it.bic }

        if (groupedByBic.size == 1 && groupedByBic.keys.first().isNullOrBlank() == false) {
            return searchResult.first()
        }

        // check if all BICs belong to the same bank but may are for different branches
        val bicsWithoutBranchCode = groupedByBic.mapNotNull { if (it.key.length >= 8) it.key.substring(0, 8) else null }.toSet()

        if (bicsWithoutBranchCode.size == 1) {
            return searchResult.firstOrNull { it.bic.endsWith("XXX") } // 'XXX' = primary office
                ?: searchResult.first()
        }

        return null
    }

    open fun searchBanksByNameBankCodeOrCity(query: String?): List<BankInfo> {
        return bankFinder.findBankByNameBankCodeOrCity(query)
    }

    open fun findRemitteesForName(name: String): List<Remittee> {
        return remitteeSearcher.findRemittees(name).map { remittee ->
            remittee.bankName = tryToFindBankName(remittee)

            remittee
        }.toSet().toList()
    }

    protected open fun tryToFindBankName(remittee: Remittee): String? {
        remittee.bic?.let { bic ->
            bankFinder.findBankByBic(bic)?.name?.let {
                return it
            }

            if (bic.length == 8) {
                bankFinder.findBankByBic(bic + "XXX")?.name?.let {
                    return it
                }
            }
        }

        remittee.iban?.let { iban ->
            if (iban.length > 12) {
                val bankCode = iban.substring(4, 12)
                return bankFinder.findBankByBankCode(bankCode).firstOrNull()?.name
            }
        }

        return null
    }


    open fun searchSelectedAccountTransactions(query: String): List<IAccountTransaction> {
        return searchAccountTransactions(query, selectedBankAccountsAccountTransactions)
    }

    open fun searchAccountTransactions(query: String, transactions: List<IAccountTransaction>): List<IAccountTransaction> {
        val queryLowercase = query.trim().toLowerCase()

        if (queryLowercase.isEmpty()) {
            return transactions
        }

        return transactions.filter {
            it.otherPartyName?.toLowerCase()?.contains(queryLowercase) == true
                    || it.usage.toLowerCase().contains(queryLowercase)
                    || it.bookingText?.toLowerCase()?.contains(queryLowercase) == true
        }
    }


    open fun getMessageLogForAccounts(customers: List<TypedCustomer>): List<String> {
        val logEntries = customers.flatMap {
            getBankingClientForAccount(it)?.messageLogWithoutSensitiveData ?: listOf()
        }

        return logEntries.map { entry ->
            MessageLogEntryDateFormatter.format(entry.time) + " " + entry.customer.bankCode + " " + entry.message
        }
    }


    open fun showAddAccountDialog() {
        router.showAddAccountDialog(this)
    }

    open fun showTransferMoneyDialog(preselectedValues: TransferMoneyData? = null) {
        router.showTransferMoneyDialog(this, preselectedValues)
    }

    open fun showSendMessageLogDialog() {
        router.showSendMessageLogDialog(this)
    }


    protected open fun getBankingClientForAccount(customer: ICustomer<*, *>): IBankingClient? {
        return bankingClientsForAccounts.get(customer as TypedCustomer)
    }


    open val selectedBankAccounts: List<TypedBankAccount>
        get() = ArrayList(selectedBankAccountsField)

    open val selectedBankAccountsAccountTransactions: List<IAccountTransaction>
        get() = getAccountTransactionsForBankAccounts(selectedBankAccounts)

    open val balanceOfSelectedBankAccounts: BigDecimal
        get() = sumBalance(selectedBankAccounts.map { it.balance })

    open val selectedBankAccountsForWhichNotAllTransactionsHaveBeenFetched: List<TypedBankAccount>
        get() = selectedBankAccounts.filter { it.haveAllTransactionsBeenFetched == false }


    open val areAllAccountSelected: Boolean
        get() = selectedAccountType == SelectedAccountType.AllAccounts

    open fun isSingleSelectedAccount(customer: TypedCustomer): Boolean {
        return selectedAccountType == SelectedAccountType.SingleAccount
                && selectedBankAccountsField.map { it.customer }.toSet().containsExactly(customer)
    }

    open fun isSingleSelectedBankAccount(bankAccount: TypedBankAccount): Boolean {
        return selectedAccountType == SelectedAccountType.SingleBankAccount
                && selectedBankAccountsField.containsExactly(bankAccount)
    }

    open fun selectedAllBankAccounts() {
        selectedAccountType = SelectedAccountType.AllAccounts

        setSelectedBankAccounts(bankAccounts)
    }

    open fun selectedAccount(customer: TypedCustomer) {
        selectedAccountType = SelectedAccountType.SingleAccount

        setSelectedBankAccounts(customer.accounts)
    }

    open fun selectedBankAccount(bankAccount: TypedBankAccount) {
        selectedAccountType = SelectedAccountType.SingleBankAccount

        setSelectedBankAccounts(listOf(bankAccount))
    }

    protected open fun setSelectedBankAccounts(bankAccounts: List<TypedBankAccount>) {
        this.selectedBankAccountsField = ArrayList(bankAccounts) // make a copy

        callSelectedBankAccountsChangedListeners(selectedBankAccountsField)
    }


    open val customers: List<TypedCustomer>
        get() = bankingClientsForAccounts.keys.toList()

    open val bankAccounts: List<TypedBankAccount>
        get() = customers.flatMap { it.accounts }

    open val allTransactions: List<IAccountTransaction>
        get() = getAccountTransactionsForBankAccounts(bankAccounts)

    open val balanceOfAllAccounts: BigDecimal
        get() = getBalanceForAccounts(customers)


    open val bankAccountsSupportingRetrievingAccountTransactions: List<TypedBankAccount>
        get() = bankAccounts.filter { it.supportsRetrievingAccountTransactions }

    open val hasBankAccountsSupportingRetrievingAccountTransactions: Boolean
        get() = doBankAccountsSupportRetrievingAccountTransactions(bankAccounts)

    open val doSelectedBankAccountsSupportRetrievingAccountTransactions: Boolean
        get() = doBankAccountsSupportRetrievingAccountTransactions(selectedBankAccounts)

    open fun doBankAccountsSupportRetrievingAccountTransactions(bankAccounts: List<TypedBankAccount>): Boolean {
        return bankAccounts.firstOrNull { it.supportsRetrievingAccountTransactions } != null
    }


    open val bankAccountsSupportingRetrievingBalance: List<TypedBankAccount>
        get() = bankAccounts.filter { it.supportsRetrievingBalance }

    open val hasBankAccountsSupportingRetrievingBalance: Boolean
        get() = doBankAccountsSupportRetrievingBalance(bankAccounts)

    open val doSelectedBankAccountsSupportRetrievingBalance: Boolean
        get() = doBankAccountsSupportRetrievingBalance(selectedBankAccounts)

    open fun doBankAccountsSupportRetrievingBalance(bankAccounts: List<TypedBankAccount>): Boolean {
        return bankAccounts.firstOrNull { it.supportsRetrievingBalance } != null
    }


    open val bankAccountsSupportingTransferringMoney: List<TypedBankAccount>
        get() = bankAccounts.filter { it.supportsTransferringMoney }

    open val hasBankAccountsSupportTransferringMoney: Boolean
        get() = doBankAccountsSupportTransferringMoney(bankAccounts)

    open val doSelectedBankAccountsSupportTransferringMoney: Boolean
        get() = doBankAccountsSupportTransferringMoney(selectedBankAccounts)

    open fun doBankAccountsSupportTransferringMoney(bankAccounts: List<TypedBankAccount>): Boolean {
        return bankAccounts.firstOrNull { it.supportsTransferringMoney } != null
    }


    protected open fun getAccountTransactionsForBankAccounts(bankAccounts: Collection<TypedBankAccount>): List<IAccountTransaction> {
        return bankAccounts.flatMap { it.bookedTransactions }.sortedByDescending { it.valueDate.millisSinceEpoch } // TODO: someday add unbooked transactions
    }

    protected open fun getBalanceForAccounts(customers: Collection<TypedCustomer>): BigDecimal {
        return customers.map { it.balance }.sum()
    }

    protected open fun sumBalance(singleBalances: Collection<BigDecimal>): BigDecimal {
        return singleBalances.sum()
    }


    open fun getTanMediaForTanProcedure(bank: TypedCustomer, tanProcedure: TanProcedure): List<TanMedium> {
        if (ChipTanTanProcedures.contains(tanProcedure.type)) {
            return bank.tanMediaSorted.filterIsInstance<TanGeneratorTanMedium>()
        }
        else if (tanProcedure.type == TanProcedureType.SmsTan) {
            return bank.tanMediaSorted.filterIsInstance<MobilePhoneTanMedium>()
        }

        return listOf()
    }


    var appSettings: AppSettings = AppSettings()
        protected set

    open fun appSettingsChanged() {
        persistAppSettings()
    }

    protected open fun persistAppSettings() {
        try {
            serializer.serializeObject(appSettings, getAppSettingsFile())
        } catch (e: Exception) {
            log.error(e) { "Could not persist AppSettings to file ${getAppSettingsFile()}" }
        }
    }

    protected open fun readAppSettings() {
        try {
            serializer.deserializeObject(getAppSettingsFile(), AppSettings::class)?.let {
                appSettings = it
            }
        } catch (e: Exception) {
            log.error(e) { "Could not read AppSettings from file ${getAppSettingsFile()}" }
        }
    }

    protected open fun getAppSettingsFile(): File {
        return File(dataFolder, "app_settings.json")
    }


    open fun addAccountsChangedListener(listener: (List<TypedCustomer>) -> Unit): Boolean {
        return accountsChangedListeners.add(listener)
    }

    open fun removeAccountsChangedListener(listener: (List<TypedCustomer>) -> Unit): Boolean {
        return accountsChangedListeners.add(listener)
    }

    protected open fun callAccountsChangedListeners() {
        val accounts = ArrayList(this.customers)

        ArrayList(accountsChangedListeners).forEach {
            it(accounts) // TODO: use RxJava for this
        }
    }


    open fun addRetrievedAccountTransactionsResponseListener(listener: (GetTransactionsResponse) -> Unit): Boolean {
        return retrievedAccountTransactionsResponseListeners.add(listener)
    }

    open fun removeRetrievedAccountTransactionsResponseListener(listener: (GetTransactionsResponse) -> Unit): Boolean {
        return retrievedAccountTransactionsResponseListeners.add(listener)
    }

    protected open fun callRetrievedAccountTransactionsResponseListener(response: GetTransactionsResponse) {
        ArrayList(retrievedAccountTransactionsResponseListeners).forEach {
            it(response) // TODO: use RxJava for this
        }
    }


    open fun addSelectedBankAccountsChangedListener(listener: (List<TypedBankAccount>) -> Unit): Boolean {
        return selectedBankAccountsChangedListeners.add(listener)
    }

    open fun removeSelectedBankAccountsChangedListener(listener: (List<TypedBankAccount>) -> Unit): Boolean {
        return selectedBankAccountsChangedListeners.add(listener)
    }

    protected open fun callSelectedBankAccountsChangedListeners(selectedBankAccounts: List<TypedBankAccount>) {
        val selectedBankAccounts = this.selectedBankAccounts

        ArrayList(selectedBankAccountsChangedListeners).forEach {
            it(selectedBankAccounts) // TODO: use RxJava for this
        }
    }

}
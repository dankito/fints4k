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
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium
import net.dankito.banking.bankfinder.IBankFinder
import net.dankito.banking.bankfinder.BankInfo
import net.dankito.banking.search.IRemitteeSearcher
import net.dankito.banking.search.Remittee
import net.dankito.banking.ui.model.moneytransfer.ExtractTransferMoneyDataFromPdfResult
import net.dankito.banking.ui.model.moneytransfer.ExtractTransferMoneyDataFromPdfResultType
import net.dankito.banking.ui.model.parameters.GetTransactionsParameter
import net.dankito.banking.ui.model.settings.AppSettings
import net.dankito.banking.util.*
import net.dankito.text.extraction.ITextExtractorRegistry
import net.dankito.text.extraction.info.invoice.IInvoiceDataExtractor
import net.dankito.text.extraction.info.invoice.InvoiceDataExtractor
import net.dankito.text.extraction.model.ErrorType
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


open class BankingPresenter(
    protected val bankingClientCreator: IBankingClientCreator,
    protected val bankFinder: IBankFinder,
    protected val dataFolder: File,
    protected val persister: IBankingPersistence,
    protected val remitteeSearcher: IRemitteeSearcher,
    protected val bankIconFinder: IBankIconFinder,
    protected val textExtractorRegistry: ITextExtractorRegistry,
    protected val router: IRouter,
    protected val invoiceDataExtractor: IInvoiceDataExtractor = InvoiceDataExtractor(),
    protected val serializer: ISerializer = JacksonJsonSerializer(),
    protected val asyncRunner: IAsyncRunner = CoroutinesAsyncRunner()
) {

    companion object {
        protected const val OneDayMillis = 24 * 60 * 60 * 1000

        protected val MessageLogEntryDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS")

        private val log = LoggerFactory.getLogger(BankingPresenter::class.java)
    }


    protected val bankingClientsForAccounts = mutableMapOf<Customer, IBankingClient>()

    protected var selectedBankAccountsField = mutableListOf<BankAccount>()

    protected var selectedAccountType = SelectedAccountType.AllAccounts

    protected var saveAccountOnNextEnterTanInvocation = false


    protected val accountsChangedListeners = mutableListOf<(List<Customer>) -> Unit>()

    protected val retrievedAccountTransactionsResponseListeners = mutableListOf<(GetTransactionsResponse) -> Unit>()

    protected val selectedBankAccountsChangedListeners = mutableListOf<(List<BankAccount>) -> Unit>()


    protected val callback: BankingClientCallback = object : BankingClientCallback {

        override fun enterTan(customer: Customer, tanChallenge: TanChallenge): EnterTanResult {
            if (saveAccountOnNextEnterTanInvocation) {
                persistAccount(customer)
                saveAccountOnNextEnterTanInvocation = false
            }

            val result = router.getTanFromUserFromNonUiThread(customer, tanChallenge, this@BankingPresenter)

            if (result.changeTanProcedureTo != null || result.changeTanMediumTo != null) { // then either selected TAN medium or procedure will change -> save account on next call to enterTan() as then changes will be visible
                saveAccountOnNextEnterTanInvocation = true
            }

            return result
        }

        override fun enterTanGeneratorAtc(tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
            return router.getAtcFromUserFromNonUiThread(tanMedium)
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
                val bankInfo = BankInfo(customer.bankName, customer.bankCode, customer.bic, "", "", "", customer.finTsServerAddress, "FinTS V3.0", null)

                val newClient = bankingClientCreator.createClient(bankInfo, customer.customerId, customer.password,
                    dataFolder, asyncRunner, callback)

                try {
                    newClient.restoreData()
                } catch (e: Exception) {
                    log.error("Could not deserialize customer data of $customer", e)
                    // TODO: show error message to user
                }

                addClientForAccount(customer, newClient)
            }

            callAccountsChangedListeners()

            selectedAllBankAccounts() // TODO: save last selected bank account(s)
        } catch (e: Exception) {
            log.error("Could not deserialize persisted accounts with persister $persister", e)
        }
    }

    protected open fun addClientForAccount(customer: Customer, client: IBankingClient) {
        bankingClientsForAccounts.put(customer, client)
    }


    // TODO: move BankInfo out of fints4k
    open fun addAccountAsync(bankInfo: BankInfo, customerId: String, pin: String, callback: (AddAccountResponse) -> Unit) {

        val newClient = bankingClientCreator.createClient(bankInfo, customerId, pin, dataFolder, asyncRunner, this.callback)

        val startDate = Date()

        newClient.addAccountAsync { response ->
            val account = response.customer

            if (response.isSuccessful) {
                addClientForAccount(account, newClient)

                selectedAccount(account)

                callAccountsChangedListeners()

                persistAccount(account)

                if (response.supportsRetrievingTransactionsOfLast90DaysWithoutTan) {
                    response.bookedTransactionsOfLast90Days.keys.forEach { bankAccount ->
                        retrievedAccountTransactions(startDate, GetTransactionsResponse(bankAccount, true, null,
                            response.bookedTransactionsOfLast90Days[bankAccount] ?: listOf(),
                        response.unbookedTransactionsOfLast90Days[bankAccount] ?: listOf(),
                            response.balances[bankAccount])
                        )
                    }
                }

                findIconForBankAsync(account)
            }

            callback(response)
        }
    }

    protected open fun findIconForBankAsync(customer: Customer) {
        asyncRunner.runAsync {
            findIconForBank(customer)
        }
    }

    protected open fun findIconForBank(customer: Customer) {
        try {
            bankIconFinder.findIconForBank(customer.bankName)?.let { bankIconUrl ->
                val bankIconFile = saveBankIconToDisk(customer, bankIconUrl)

                customer.iconUrl = "file://" + bankIconFile.absolutePath // without 'file://' Android will not find it

                persistAccount(customer)

                callAccountsChangedListeners()
            }
        } catch (e: Exception) {
            log.error("Could not get icon for bank ${customer.bankName}", e)
        }
    }

    protected open fun saveBankIconToDisk(customer: Customer, bankIconUrl: String): File {
        val bankIconsDir = File(dataFolder, "bank_icons")
        bankIconsDir.mkdirs()

        val extension = getIconFileExtension(bankIconUrl)
        val bankIconFile = File(bankIconsDir, customer.bankCode + if (extension != null) (".$extension") else "")

        URL(bankIconUrl).openConnection().getInputStream().buffered().use { iconInputStream ->
            FileOutputStream(bankIconFile).use { fileOutputStream ->
                iconInputStream.copyTo(fileOutputStream)
            }
        }

        return bankIconFile
    }

    protected open fun getIconFileExtension(bankIconUrl: String): String? {
        try {
            var iconFilename = File(bankIconUrl).name
            if (iconFilename.contains('?')) {
                iconFilename = iconFilename.substring(0, iconFilename.indexOf('?'))
            }

            val extension = File(iconFilename).extension
            if (extension.isNotBlank()) {
                return extension
            }
        } catch (e: Exception) {
            log.info("Could not get icon file extension from url '$bankIconUrl'", e)
        }

        return null
    }


    open fun deleteAccount(customer: Customer) {
        val wasSelected = isSingleSelectedAccount(customer) or // either account or one of its bank accounts is currently selected
                (customer.accounts.firstOrNull { isSingleSelectedBankAccount(it) } != null)

        bankingClientsForAccounts.remove(customer)

        persister.deleteAccount(customer, customers)

        callAccountsChangedListeners()

        if (wasSelected || areAllAccountSelected) { // to update displayed account transactions as transactions of yet deleted accounts have to be removed
            selectedAllBankAccounts()
        }
    }


    open fun fetchAccountTransactionsAsync(customer: Customer,
                                           callback: (GetTransactionsResponse) -> Unit) {

        customer.accounts.forEach { bankAccount ->
            if (bankAccount.supportsRetrievingAccountTransactions) {
                fetchAccountTransactionsAsync(bankAccount, callback) // TODO: use a synchronous version of fetchAccountTransactions() so that all bank accounts get handled serially
            }
        }
    }

    open fun fetchAccountTransactionsAsync(bankAccount: BankAccount,
                                           callback: (GetTransactionsResponse) -> Unit) {

        fetchAccountTransactionsAsync(bankAccount, null, false, callback)
    }

    open fun fetchAccountTransactionsAsync(bankAccount: BankAccount, fromDate: Date?, abortIfTanIsRequired: Boolean = false,
                                           callback: (GetTransactionsResponse) -> Unit) {

        getBankingClientForAccount(bankAccount.customer)?.let { client ->
            val startDate = Date()

            client.getTransactionsAsync(bankAccount, GetTransactionsParameter(true, fromDate, null, abortIfTanIsRequired, { receivedAccountsTransactionChunk(bankAccount, it) } )) { response ->

                if (response.tanRequiredButWeWereToldToAbortIfSo == false) { // don't call retrievedAccountTransactions() if aborted due to TAN required but we told client to abort if so
                    retrievedAccountTransactions(startDate, response)
                }

                callback(response)
            }
        }
    }

    open fun updateAccountsTransactionsAsync(callback: (GetTransactionsResponse) -> Unit) {
        updateAccountsTransactionsAsync(false, callback)
    }

    open fun updateAccountsTransactionsIfNoTanIsRequiredAsync() {
        updateAccountsTransactionsAsync(true) { }
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

    protected open fun updateBankAccountTransactionsAsync(bankAccount: BankAccount, abortIfTanIsRequired: Boolean, callback: (GetTransactionsResponse) -> Unit) {
        val fromDate = bankAccount.lastRetrievedTransactionsTimestamp?.let { Date(it.time - OneDayMillis) } // one day before last received transactions

        fetchAccountTransactionsAsync(bankAccount, fromDate, abortIfTanIsRequired, callback)
    }

    protected open fun retrievedAccountTransactions(startDate: Date, response: GetTransactionsResponse) {
        if (response.isSuccessful) {
            response.bankAccount.lastRetrievedTransactionsTimestamp = startDate

            updateAccountTransactionsAndBalances(response)
        }

        callRetrievedAccountTransactionsResponseListener(response)
    }

    protected open fun receivedAccountsTransactionChunk(bankAccount: BankAccount, accountTransactionsChunk: List<AccountTransaction>) {
        if (accountTransactionsChunk.isNotEmpty()) {
            bankAccount.addBookedTransactions(accountTransactionsChunk)

            callRetrievedAccountTransactionsResponseListener(GetTransactionsResponse(bankAccount, true, null, accountTransactionsChunk))
        }
    }

    protected open fun updateAccountTransactionsAndBalances(response: GetTransactionsResponse) {

        val bankAccount = response.bankAccount

        bankAccount.addBookedTransactions(response.bookedTransactions)

        bankAccount.addUnbookedTransactions(response.unbookedTransactions)

        response.balance?.let {
            bankAccount.balance = it
        }

        persistAccount(bankAccount.customer) // only needed because of balance
        persistAccountTransactions(bankAccount, response.bookedTransactions, response.unbookedTransactions)
    }

    open fun formatAmount(amount: BigDecimal): String {
        return String.format("%.02f", amount)
    }


    protected open fun persistAccount(customer: Customer) {
        persister.saveOrUpdateAccount(customer, customers)
    }

    protected open fun persistAccountTransactions(bankAccount: BankAccount, bookedTransactions: List<AccountTransaction>, unbookedTransactions: List<Any>) {
        persister.saveOrUpdateAccountTransactions(bankAccount, bookedTransactions)

        // TODO: someday also persist unbooked transactions
    }


    open fun transferMoneyAsync(bankAccount: BankAccount, data: TransferMoneyData, callback: (BankingClientResponse) -> Unit) {
        getBankingClientForAccount(bankAccount.customer)?.let { client ->
            client.transferMoneyAsync(data, bankAccount) { response ->
                if (response.isSuccessful) {
                    updateBankAccountTransactionsAsync(bankAccount, true) { }
                }

                callback(response)
            }
        }
    }

    open fun transferMoneyWithDataFromPdf(pdf: File): ExtractTransferMoneyDataFromPdfResult {
        val extractionResult = textExtractorRegistry.extractTextWithBestExtractorForFile(pdf)

        if (extractionResult.couldExtractText == false || extractionResult.text == null) {
            val resultType = if (extractionResult.error?.type == ErrorType.NoExtractorFoundForFileType) ExtractTransferMoneyDataFromPdfResultType.NotASearchablePdf
                        else ExtractTransferMoneyDataFromPdfResultType.CouldNotExtractText
            return ExtractTransferMoneyDataFromPdfResult(resultType, extractionResult.error?.exception)
        }
        else {
            extractionResult.text?.let { extractedText ->
                val invoiceData = invoiceDataExtractor.extractInvoiceData(extractedText)

                if (invoiceData.potentialTotalAmount != null || invoiceData.potentialIban != null) { // if at least an amount or IBAN could get extracted
                    val transferMoneyData = TransferMoneyData("",
                        invoiceData.potentialIban ?: "",
                        invoiceData.potentialBic ?: "",
                        invoiceData.potentialTotalAmount?.amount ?: BigDecimal.ZERO, "")
                    showTransferMoneyDialog(null, transferMoneyData)
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

        if (groupedByBic.size == 1) {
            return searchResult.first()
        }

        // check if all BICs belong to the same bank but may are for different branches
        val bicsWithoutBranchCode = groupedByBic.map { it.key.substring(0, 8) }.toSet()

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
            remittee.bic?.let { bic ->
                remittee.bankName = bankFinder.findBankByBic(bic)?.name
            }

            remittee
        }
    }


    open fun searchSelectedAccountTransactions(query: String): List<AccountTransaction> {
        val queryLowercase = query.trim().toLowerCase()

        if (queryLowercase.isEmpty()) {
            return selectedBankAccountsAccountTransactions
        }

        return selectedBankAccountsAccountTransactions.filter {
            it.otherPartyName?.toLowerCase()?.contains(queryLowercase) == true
                    || it.usage.toLowerCase().contains(queryLowercase)
                    || it.bookingText?.toLowerCase()?.contains(queryLowercase) == true
        }
    }


    open fun getMessageLogForAccounts(customers: List<Customer>): List<String> {
        val logEntries = customers.flatMap {
            getBankingClientForAccount(it)?.messageLogWithoutSensitiveData ?: listOf()
        }

        return logEntries.map { entry ->
            MessageLogEntryDateFormat.format(entry.time) + " " + entry.customer.bankCode + " " + entry.message
        }
    }


    open fun showAddAccountDialog() {
        router.showAddAccountDialog(this)
    }

    open fun showTransferMoneyDialog(preselectedBankAccount: BankAccount? = null, preselectedValues: TransferMoneyData? = null) {
        router.showTransferMoneyDialog(this, preselectedBankAccount, preselectedValues)
    }

    open fun showSendMessageLogDialog() {
        router.showSendMessageLogDialog(this)
    }


    protected open fun getBankingClientForAccount(customer: Customer): IBankingClient? {
        return bankingClientsForAccounts.get(customer)
    }


    open val selectedBankAccounts: List<BankAccount>
        get() = ArrayList(selectedBankAccountsField)

    open val selectedBankAccountsAccountTransactions: List<AccountTransaction>
        get() = getAccountTransactionsForBankAccounts(selectedBankAccounts)

    open val balanceOfSelectedBankAccounts: BigDecimal
        get() = sumBalance(selectedBankAccounts.map { it.balance })


    open val areAllAccountSelected: Boolean
        get() = selectedAccountType == SelectedAccountType.AllAccounts

    open fun isSingleSelectedAccount(customer: Customer): Boolean {
        return selectedAccountType == SelectedAccountType.SingleAccount
                && selectedBankAccountsField.map { it.customer }.toSet().containsExactly(customer)
    }

    open fun isSingleSelectedBankAccount(bankAccount: BankAccount): Boolean {
        return selectedAccountType == SelectedAccountType.SingleBankAccount
                && selectedBankAccountsField.containsExactly(bankAccount)
    }

    open fun selectedAllBankAccounts() {
        selectedAccountType = SelectedAccountType.AllAccounts

        setSelectedBankAccounts(bankAccounts)
    }

    open fun selectedAccount(customer: Customer) {
        selectedAccountType = SelectedAccountType.SingleAccount

        setSelectedBankAccounts(customer.accounts)
    }

    open fun selectedBankAccount(bankAccount: BankAccount) {
        selectedAccountType = SelectedAccountType.SingleBankAccount

        setSelectedBankAccounts(listOf(bankAccount))
    }

    protected open fun setSelectedBankAccounts(bankAccounts: List<BankAccount>) {
        this.selectedBankAccountsField = ArrayList(bankAccounts) // make a copy

        callSelectedBankAccountsChangedListeners(selectedBankAccountsField)
    }


    open val customers: List<Customer>
        get() = bankingClientsForAccounts.keys.toList()

    open val bankAccounts: List<BankAccount>
        get() = customers.flatMap { it.accounts }

    open val allTransactions: List<AccountTransaction>
        get() = getAccountTransactionsForBankAccounts(bankAccounts)

    open val balanceOfAllAccounts: BigDecimal
        get() = getBalanceForAccounts(customers)


    open val bankAccountsSupportingRetrievingAccountTransactions: List<BankAccount>
        get() = bankAccounts.filter { it.supportsRetrievingAccountTransactions }

    open val hasBankAccountsSupportingRetrievingAccountTransactions: Boolean
        get() = doBankAccountsSupportRetrievingAccountTransactions(bankAccounts)

    open val doSelectedBankAccountsSupportRetrievingAccountTransactions: Boolean
        get() = doBankAccountsSupportRetrievingAccountTransactions(selectedBankAccounts)

    open fun doBankAccountsSupportRetrievingAccountTransactions(bankAccounts: List<BankAccount>): Boolean {
        return bankAccounts.firstOrNull { it.supportsRetrievingAccountTransactions } != null
    }


    open val bankAccountsSupportingRetrievingBalance: List<BankAccount>
        get() = bankAccounts.filter { it.supportsRetrievingBalance }

    open val hasBankAccountsSupportingRetrievingBalance: Boolean
        get() = doBankAccountsSupportRetrievingBalance(bankAccounts)

    open val doSelectedBankAccountsSupportRetrievingBalance: Boolean
        get() = doBankAccountsSupportRetrievingBalance(selectedBankAccounts)

    open fun doBankAccountsSupportRetrievingBalance(bankAccounts: List<BankAccount>): Boolean {
        return bankAccounts.firstOrNull { it.supportsRetrievingBalance } != null
    }


    open val bankAccountsSupportingTransferringMoney: List<BankAccount>
        get() = bankAccounts.filter { it.supportsTransferringMoney }

    open val hasBankAccountsSupportTransferringMoney: Boolean
        get() = doBankAccountsSupportTransferringMoney(bankAccounts)

    open val doSelectedBankAccountsSupportTransferringMoney: Boolean
        get() = doBankAccountsSupportTransferringMoney(selectedBankAccounts)

    open fun doBankAccountsSupportTransferringMoney(bankAccounts: List<BankAccount>): Boolean {
        return bankAccounts.firstOrNull { it.supportsTransferringMoney } != null
    }


    protected open fun getAccountTransactionsForBankAccounts(bankAccounts: Collection<BankAccount>): List<AccountTransaction> {
        return bankAccounts.flatMap { it.bookedTransactions }.sortedByDescending { it.valueDate } // TODO: someday add unbooked transactions
    }

    protected open fun getBalanceForAccounts(customers: Collection<Customer>): BigDecimal {
        return customers.map { it.balance }.fold(BigDecimal.ZERO) { acc, e -> acc + e }
    }

    protected open fun sumBalance(singleBalances: Collection<BigDecimal>): BigDecimal {
        return singleBalances.fold(BigDecimal.ZERO) { acc, e -> acc + e }
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
            log.error("Could not persist AppSettings to file ${getAppSettingsFile()}", e)
        }
    }

    protected open fun readAppSettings() {
        try {
            serializer.deserializeObject(getAppSettingsFile(), AppSettings::class.java)?.let {
                appSettings = it
            }
        } catch (e: Exception) {
            log.error("Could not read AppSettings from file ${getAppSettingsFile()}", e)
        }
    }

    protected open fun getAppSettingsFile(): File {
        return File(dataFolder, "app_settings.json")
    }


    open fun addAccountsChangedListener(listener: (List<Customer>) -> Unit): Boolean {
        return accountsChangedListeners.add(listener)
    }

    open fun removeAccountsChangedListener(listener: (List<Customer>) -> Unit): Boolean {
        return accountsChangedListeners.add(listener)
    }

    protected open fun callAccountsChangedListeners() {
        val accounts = this.customers

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


    open fun addSelectedBankAccountsChangedListener(listener: (List<BankAccount>) -> Unit): Boolean {
        return selectedBankAccountsChangedListeners.add(listener)
    }

    open fun removeSelectedBankAccountsChangedListener(listener: (List<BankAccount>) -> Unit): Boolean {
        return selectedBankAccountsChangedListeners.add(listener)
    }

    protected open fun callSelectedBankAccountsChangedListeners(selectedBankAccounts: List<BankAccount>) {
        val selectedBankAccounts = this.selectedBankAccounts

        ArrayList(selectedBankAccountsChangedListeners).forEach {
            it(selectedBankAccounts) // TODO: use RxJava for this
        }
    }

}
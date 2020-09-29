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
import net.dankito.banking.search.ITransactionPartySearcher
import net.dankito.banking.search.NoOpTransactionPartySearcher
import net.dankito.banking.search.TransactionParty
import net.dankito.banking.ui.comparator.BankAccountComparator
import net.dankito.banking.ui.model.mapper.DefaultModelCreator
import net.dankito.banking.ui.model.mapper.IModelCreator
import net.dankito.banking.ui.model.moneytransfer.ExtractTransferMoneyDataFromPdfResult
import net.dankito.banking.ui.model.moneytransfer.ExtractTransferMoneyDataFromPdfResultType
import net.dankito.banking.ui.model.parameters.GetTransactionsParameter
import net.dankito.banking.ui.model.settings.AppSettings
import net.dankito.banking.ui.model.settings.TanMethodSettings
import net.dankito.banking.ui.model.tan.*
import net.dankito.banking.ui.util.CurrencyInfo
import net.dankito.banking.ui.util.CurrencyInfoProvider
import net.dankito.banking.ui.util.ICurrencyInfoProvider
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
    protected val transactionPartySearcher: ITransactionPartySearcher = NoOpTransactionPartySearcher(),
    protected val bankIconFinder: IBankIconFinder = NoOpBankIconFinder(),
    protected val textExtractorRegistry: ITextExtractorRegistry = NoOpTextExtractorRegistry(),
    protected val invoiceDataExtractor: IInvoiceDataExtractor = NoOpInvoiceDataExtractor(),
    protected val currencyInfoProvider: ICurrencyInfoProvider = CurrencyInfoProvider(),
    protected val serializer: ISerializer = NoOpSerializer(),
    protected val asyncRunner: IAsyncRunner = CoroutinesAsyncRunner()
) {

    companion object {
        val ChipTanTanMethods = listOf(TanMethodType.ChipTanManuell, TanMethodType.ChipTanFlickercode, TanMethodType.ChipTanUsb,
            TanMethodType.ChipTanQrCode, TanMethodType.ChipTanPhotoTanMatrixCode)

        val QrCodeTanMethods = listOf(TanMethodType.ChipTanQrCode, TanMethodType.QrCode)

        val PhotoTanMethods = listOf(TanMethodType.ChipTanPhotoTanMatrixCode, TanMethodType.photoTan)

        val OpticalTanMethods = listOf(TanMethodType.ChipTanFlickercode, TanMethodType.ChipTanQrCode,
            TanMethodType.ChipTanPhotoTanMatrixCode, TanMethodType.photoTan, TanMethodType.QrCode)

        protected const val OneDayMillis = 24 * 60 * 60 * 1000L

        protected val ShortDateStyleDateFormatter = DateFormatter(DateFormatStyle.Short)

        protected val MediumDateStyleDateFormatter = DateFormatter(DateFormatStyle.Medium)

        protected val MessageLogEntryDateFormatter = DateFormatter("yyyy.MM.dd HH:mm:ss.SSS")

        private val log = LoggerFactory.getLogger(BankingPresenter::class)
    }


    protected val bankingClientsForBanks = mutableMapOf<TypedBankData, IBankingClient>()

    protected var _selectedAccounts = mutableListOf<TypedBankAccount>()

    protected var selectedAccountType = SelectedAccountType.AllAccounts

    protected var saveAccountOnNextEnterTanInvocation = false


    protected val banksChangedListeners = mutableListOf<(List<TypedBankData>) -> Unit>()

    protected val retrievedAccountTransactionsResponseListeners = mutableListOf<(GetTransactionsResponse) -> Unit>()

    protected val selectedAccountsChangedListeners = mutableListOf<(List<TypedBankAccount>) -> Unit>()


    protected val callback: BankingClientCallback = object : BankingClientCallback {

        override fun enterTan(bank: TypedBankData, tanChallenge: TanChallenge, callback: (EnterTanResult) -> Unit) {
            if (saveAccountOnNextEnterTanInvocation) {
                persistBankOffUiThread(bank)
                saveAccountOnNextEnterTanInvocation = false
            }

            router.getTanFromUserFromNonUiThread(bank, tanChallenge, this@BankingPresenter) { result ->
                if (result.changeTanMethodTo != null || result.changeTanMediumTo != null) { // then either selected TAN medium or method will change -> save account on next call to enterTan() as then changes will be visible
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
            readPersistedBanks()

            updateAllAccountsTransactionsAsync()
        }

        // preloadBankList asynchronously; on Android it takes approximately 18 seconds till banks are indexed for first time -> do it as early as possible
        asyncRunner.runAsync {
            bankFinder.preloadBankList()
        }
    }


    protected open fun readPersistedBanks() {
        try {
            val deserializedBanks = persister.readPersistedBanks()

            deserializedBanks.forEach { bank ->
                val newClient = bankingClientCreator.createClient(bank, dataFolder, asyncRunner, callback)

                addClientForBank(bank, newClient)

                bank.accounts.forEach { account ->
                    if (account.haveAllTransactionsBeenRetrieved == false && didFetchAllTransactionsStoredOnBankServer(account, listOf())) {
                        account.haveAllTransactionsBeenRetrieved = true // no need to save account, just delays app start-up, as even if account doesn't get saved during app run, haveAllTransactionsBeenRetrieved gets restored on next app run
                    }
                }
            }

            callBanksChangedListeners()

            selectedAllAccounts() // TODO: save last selected bank account(s)
        } catch (e: Exception) {
            log.error(e) { "Could not deserialize persisted banks with persister $persister" }
        }
    }

    protected open fun addClientForBank(bank: TypedBankData, client: IBankingClient) {
        bankingClientsForBanks.put(bank, client)
    }


    // TODO: move BankInfo out of fints4k
    open fun addAccountAsync(bankInfo: BankInfo, userName: String, password: String, callback: (AddAccountResponse) -> Unit) {
        val bank = modelCreator.createBank(bankInfo.bankCode, userName, password, bankInfo.pinTanAddress ?: "", bankInfo.name, bankInfo.bic, "")

        val newClient = bankingClientCreator.createClient(bank, dataFolder, asyncRunner, this.callback)

        val startDate = Date()

        newClient.addAccountAsync { response ->
            if (response.successful) {
                val bank = response.bank
                bank.displayIndex = allBanks.size

                addClientForBank(bank, newClient)

                selectedBank(bank)

                callBanksChangedListeners()

                persistBankOffUiThread(bank)

                response.retrievedData.forEach { retrievedData ->
                    retrievedAccountTransactions(GetTransactionsResponse(retrievedData), startDate, false)
                }

                findIconForBankAsync(bank)
            }

            callback(response)
        }
    }

    protected open fun findIconForBankAsync(bank: TypedBankData) {
        bankIconFinder.findIconForBankAsync(bank.bankName) { bankIconUrl ->
            bankIconUrl?.let {
                try {
                    handleFindIconForBankResult(bank, bankIconUrl)
                } catch (e: Exception) {
                    log.error(e) { "Could not get icon for bank ${bank.bankName}" }
                }
            }
        }
    }

    protected open fun handleFindIconForBankResult(bank: TypedBankData, bankIconUrl: String) {
        val bankIconFile = saveBankIconToDisk(bank, bankIconUrl)

        var iconFilePath = bankIconFile.getAbsolutePath()

        if (iconFilePath.startsWith("file://", true) == false) {
            iconFilePath = "file://" + iconFilePath // without 'file://' Android will not find it
        }

        bank.iconUrl = iconFilePath

        persistBankOffUiThread(bank)

        callBanksChangedListeners()
    }

    protected open fun saveBankIconToDisk(bank: TypedBankData, bankIconUrl: String): File {
        val bankIconsDir = File(dataFolder, "bank_icons")
        bankIconsDir.mkdirs()

        val extension = getIconFileExtension(bankIconUrl)
        val bankIconFile = File(bankIconsDir, bank.bankCode + if (extension != null) (".$extension") else "")

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


    open fun deleteAccount(bank: TypedBankData) {
        asyncRunner.runAsync {
            deleteAccountOffUiThread(bank)
        }
    }

    protected open fun deleteAccountOffUiThread(bank: TypedBankData) {
        val wasSelected = isSingleSelectedBank(bank) or // either bank or one of its bank accounts is currently selected
                (bank.accounts.firstOrNull { isSingleSelectedAccount(it) } != null)

        val client = bankingClientsForBanks.remove(bank)

        val displayIndex = bank.displayIndex

        persister.deleteBank(bank, allBanks)

        val sortedBanks = allBanks.sortedByDisplayIndex()
        for (i in IntRange(displayIndex, sortedBanks.size - 1)) {
            val bank = sortedBanks[i]
            bank.displayIndex = i
            bankDisplayIndexUpdated(bank)
        }

        client?.deletedBank(bank, allBanks.firstOrNull { it.userName == bank.userName && it.bankCode == bank.bankCode} == null)

        callBanksChangedListeners()

        if (wasSelected || areAllAccountSelected) { // to update displayed account transactions as transactions of yet deleted accounts have to be removed
            selectedAllAccounts()
        }
    }


    /**
     * If for an account already all transactions have been fetch, then latest transactions get fetched.
     *
     * Otherwise all transactions are fetched.
     */
    open fun fetchTransactionsOfSelectedAccounts(callback: ((GetTransactionsResponse) -> Unit)? = null) {
        selectedAccounts.forEach { account ->
            if (account.haveAllTransactionsBeenRetrieved) {
                updateAccountTransactionsAsync(account, false, callback)
            }
            else {
                fetchAllAccountTransactionsAsync(account, callback)
            }
        }
    }

    open fun fetchAllTransactionsOfSelectedAccounts(callback: ((GetTransactionsResponse) -> Unit)? = null) {
        selectedAccountsForWhichNotAllTransactionsHaveBeenFetched.forEach { account ->
            fetchAllAccountTransactionsAsync(account, callback)
        }
    }

    open fun fetchAllAccountTransactionsAsync(bank: TypedBankData,
                                              callback: ((GetTransactionsResponse) -> Unit)? = null) {

        bank.accounts.forEach { account ->
            if (account.supportsRetrievingAccountTransactions) {
                fetchAllAccountTransactionsAsync(account, callback) // TODO: use a synchronous version of fetchAccountTransactions() so that all bank accounts get handled serially
            }
        }
    }

    protected open fun fetchAllAccountTransactionsAsync(account: TypedBankAccount,
                                              callback: ((GetTransactionsResponse) -> Unit)? = null) {

        fetchAccountTransactionsAsync(account, null, false, callback)
    }

    protected open fun fetchAccountTransactionsAsync(account: TypedBankAccount, fromDate: Date?, abortIfTanIsRequired: Boolean = false,
                                           callback: ((GetTransactionsResponse) -> Unit)? = null) {

        getBankingClientForBank(account.bank)?.let { client ->
            val startDate = Date()

            client.getTransactionsAsync(GetTransactionsParameter(account,true, fromDate, null, abortIfTanIsRequired, { receivedAccountTransactionChunk(account, it) } )) { response ->

                if (response.tanRequiredButWeWereToldToAbortIfSo == false) { // don't call retrievedAccountTransactions() if aborted due to TAN required but we told client to abort if so
                    retrievedAccountTransactions(response, startDate, fromDate == null)
                }

                callback?.invoke(response)
            }
        }
    }


    open fun updateAllAccountsTransactionsAsync(callback: ((GetTransactionsResponse) -> Unit)? = null) {
        val accountsToUpdate = allAccounts.filter { it.hideAccount == false && it.updateAccountAutomatically }

        updateAccountsTransactionsAsync(accountsToUpdate, true, callback)
    }

    open fun updateSelectedAccountsTransactionsAsync(callback: ((GetTransactionsResponse) -> Unit)? = null) {
        var accountsToUpdate = selectedAccounts.filter { it.updateAccountAutomatically }
        if (accountsToUpdate.isEmpty() && (selectedAccountType == SelectedAccountType.SingleAccount
                    || (selectedAccountType == SelectedAccountType.SingleBank && selectedAccounts.size == 1))) {
            accountsToUpdate = selectedAccounts
        }

        updateAccountsTransactionsAsync(accountsToUpdate, false, callback)
    }


    protected open fun updateAccountsTransactionsAsync(accounts: List<TypedBankAccount>, abortIfTanIsRequired: Boolean = false, callback: ((GetTransactionsResponse) -> Unit)? = null) {
        accounts.forEach { account ->
            if (account.supportsRetrievingAccountTransactions) {
                updateAccountTransactionsAsync(account, abortIfTanIsRequired, callback)
            }
        }
    }

    open fun updateAccountTransactionsAsync(account: TypedBankAccount, abortIfTanIsRequired: Boolean = false, callback: ((GetTransactionsResponse) -> Unit)? = null) {
        val fromDate = account.retrievedTransactionsUpTo?.let { Date(it.millisSinceEpoch - OneDayMillis) } // one day before last received transactions

        fetchAccountTransactionsAsync(account, fromDate, abortIfTanIsRequired, callback)
    }

    protected open fun retrievedAccountTransactions(response: GetTransactionsResponse, startDate: Date, didFetchAllTransactions: Boolean) {
        if (response.successful) {
            response.retrievedData.forEach { retrievedData ->
                val account = retrievedData.account
                account.retrievedTransactionsUpTo = startDate
                if (account.retrievedTransactionsFromOn == null || retrievedData.retrievedTransactionsFrom?.isBefore(account.retrievedTransactionsFromOn!!) == true) {
                    account.retrievedTransactionsFromOn = retrievedData.retrievedTransactionsFrom
                }

                if (didFetchAllTransactions || didFetchAllTransactionsStoredOnBankServer(account, retrievedData.bookedTransactions)) {
                    account.haveAllTransactionsBeenRetrieved = true
                }

                updateAccountTransactionsAndBalances(retrievedData)
            }
        }

        callRetrievedAccountTransactionsResponseListener(response)
    }

    protected open fun didFetchAllTransactionsStoredOnBankServer(account: IBankAccount<IAccountTransaction>, fetchedTransactions: Collection<IAccountTransaction>): Boolean {
        account.bank.countDaysForWhichTransactionsAreKept?.let { countDaysForWhichTransactionsAreKept ->
            (account.retrievedTransactionsFromOn ?: getDateOfFirstRetrievedTransaction(account.bookedTransactions) ?: getDateOfFirstRetrievedTransaction(fetchedTransactions))?.let { retrievedTransactionsFromOn ->
                val dayOfFirstTransactionStoredOnBankServer = Date(Date.today.millisSinceEpoch - countDaysForWhichTransactionsAreKept * OneDayMillis)

                return retrievedTransactionsFromOn.isBeforeOrEquals(dayOfFirstTransactionStoredOnBankServer)
            }
        }

        return false
    }

    protected open fun getDateOfFirstRetrievedTransaction(transactions: Collection<IAccountTransaction>): Date? {
        return transactions.map { it.valueDate }.minBy { it.millisSinceEpoch }
    }

    protected open fun receivedAccountTransactionChunk(account: TypedBankAccount, transactionsChunk: List<IAccountTransaction>) {
        if (transactionsChunk.isNotEmpty()) {
            asyncRunner.runAsync { // don't block retrieving next chunk by blocked saving to db / json
                updateAccountTransactions(account, transactionsChunk)

                callRetrievedAccountTransactionsResponseListener(GetTransactionsResponse(RetrievedAccountData(account, true, null, transactionsChunk, listOf(), null, null)))
            }
        }
    }

    protected open fun updateAccountTransactionsAndBalances(retrievedData: RetrievedAccountData) {
        updateAccountTransactions(retrievedData.account, retrievedData.bookedTransactions, retrievedData.unbookedTransactions)

        retrievedData.balance?.let {
            updateBalance(retrievedData.account, it)
        }
    }

    protected open fun updateAccountTransactions(account: TypedBankAccount, bookedTransactions: Collection<IAccountTransaction>, unbookedTransactions: List<Any>? = null) {
        val knownAccountTransactions = account.bookedTransactions.map { it.transactionIdentifier }

        val newBookedTransactions = bookedTransactions.filterNot { knownAccountTransactions.contains(it.transactionIdentifier) }
        account.addBookedTransactions(newBookedTransactions)

        unbookedTransactions?.let {
            account.addUnbookedTransactions(unbookedTransactions)
        }

        persistAccountTransactionsOffUiThread(account, newBookedTransactions)
    }

    protected open fun updateBalance(account: TypedBankAccount, balance: BigDecimal) {
        account.balance = balance

        persistBankOffUiThread(account.bank)
    }


    open fun formatAmount(amount: BigDecimal): String { // for languages not supporting default parameters
        return formatAmount(amount, null)
    }

    open fun formatAmount(amount: BigDecimal, currencyIsoCode: String? = null): String {
        val isoCode = currencyIsoCode ?: currencyIsoCodeOfSelectedAccounts

        return formatAmount(amount, currencyInfoProvider.getInfoForIsoCode(isoCode) ?: currencyInfoProvider.userDefaultCurrencyInfo)
    }

    open fun formatAmount(amount: BigDecimal, currencyInfo: CurrencyInfo): String {
        return amount.format(currencyInfo.defaultFractionDigits) + " " + currencyInfo.symbol
    }


    open fun formatToShortDate(date: Date): String {
        return ShortDateStyleDateFormatter.format(date)
    }

    open fun formatToMediumDate(date: Date): String {
        return MediumDateStyleDateFormatter.format(date)
    }


    open fun allBanksUpdated() {
        allBanks.forEach { bank ->
            bankDisplayIndexUpdated(bank)
        }
    }

    open fun bankDisplayIndexUpdated(bank: TypedBankData) {
        persistBankAsync(bank)

        callBanksChangedListeners()
    }

    open fun bankUpdated(bank: TypedBankData) {
        persistBankAsync(bank)

        callBanksChangedListeners()

        getBankingClientForBank(bank)?.dataChanged(bank)
    }

    open fun accountUpdated(account: TypedBankAccount) {
        persistBankAsync(account.bank)

        callBanksChangedListeners()
    }

    open fun doNotShowStrikingFetchAllTransactionsViewAnymore(accounts: List<TypedBankAccount>) {
        accounts.forEach { account ->
            account.doNotShowStrikingFetchAllTransactionsView = true

            persistBankAsync(account.bank)
        }

        callBanksChangedListeners()
    }

    protected open fun persistBankAsync(bank: IBankData<*, *>) {
        asyncRunner.runAsync {
            persistBankOffUiThread(bank)
        }
    }

    /**
     * Ensure that this method only gets called off UI thread (at least for Android Room db) as otherwise it may blocks UI thread.
     */
    protected open fun persistBankOffUiThread(bank: IBankData<*, *>) {
        persister.saveOrUpdateBank(bank as TypedBankData, allBanks)
    }

    /**
     * Ensure that this method only gets called off UI thread (at least for Android Room db) as otherwise it may blocks UI thread.
     */
    protected open fun persistAccountTransactionsOffUiThread(account: TypedBankAccount, bookedTransactions: List<IAccountTransaction>) {
        persister.saveOrUpdateAccountTransactions(account, bookedTransactions)
    }


    open fun transferMoneyAsync(data: TransferMoneyData, callback: (BankingClientResponse) -> Unit) {
        val account = data.account

        getBankingClientForBank(account.bank)?.let { client ->
            client.transferMoneyAsync(data) { response ->
                if (response.successful) {
                    updateAccountTransactionsAsync(account, true)
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
                        allAccounts.first(), "",
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
            .sortedBy { it.name }
    }

    open fun findRecipientsForName(name: String): List<TransactionParty> {
        return transactionPartySearcher.findTransactionParty(name).map { recipient ->
            recipient.bankName = tryToFindBankName(recipient)

            recipient
        }.toSet().toList()
    }

    protected open fun tryToFindBankName(transactionParty: TransactionParty): String? {
        transactionParty.bic?.let { bic ->
            bankFinder.findBankByBic(bic)?.name?.let {
                return it
            }

            if (bic.length == 8) {
                bankFinder.findBankByBic(bic + "XXX")?.name?.let {
                    return it
                }
            }
        }

        transactionParty.iban?.let { iban ->
            if (iban.length > 12) {
                val bankCode = iban.substring(4, 12)
                return bankFinder.findBankByBankCode(bankCode).firstOrNull()?.name
            }
        }

        return null
    }


    open fun searchSelectedAccountTransactions(query: String): List<IAccountTransaction> {
        return searchAccountTransactions(query, selectedAccountsTransactions)
    }

    open fun searchAccountTransactions(query: String, transactions: List<IAccountTransaction>): List<IAccountTransaction> {
        val queryLowercase = query.trim().toLowerCase()

        if (queryLowercase.isEmpty()) {
            return transactions
        }

        return transactions.filter {
            it.otherPartyName?.toLowerCase()?.contains(queryLowercase) == true
                    || it.reference.toLowerCase().contains(queryLowercase)
                    || it.bookingText?.toLowerCase()?.contains(queryLowercase) == true
        }
    }


    open fun getMessageLogForAccounts(banks: List<TypedBankData>): List<String> {
        val logEntries = banks.flatMap {
            getBankingClientForBank(it)?.messageLogWithoutSensitiveData ?: listOf()
        }

        return logEntries.map { entry ->
            MessageLogEntryDateFormatter.format(entry.time) + " " + entry.bank.bankCode + " " + entry.message
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


    protected open fun getBankingClientForBank(bank: IBankData<*, *>): IBankingClient? {
        return bankingClientsForBanks.get(bank as TypedBankData)
    }


    open val selectedAccounts: List<TypedBankAccount>
        get() = ArrayList(_selectedAccounts)

    open val selectedAccountsTransactions: List<IAccountTransaction>
        get() = getTransactionsForAccounts(selectedAccounts)

    open val balanceOfSelectedAccounts: BigDecimal
        get() = sumBalance(selectedAccounts.map { it.balance })

    open val currencyIsoCodeOfSelectedAccounts: String
        get() = currencyIsoCodeOfAccounts(selectedAccounts)

    open val currencySymbolOfSelectedAccounts: String
        get() = currencyInfoProvider.getCurrencySymbolForIsoCodeOrEuro(currencyIsoCodeOfSelectedAccounts)

    open val selectedAccountsForWhichNotAllTransactionsHaveBeenFetched: List<TypedBankAccount>
        get() = selectedAccounts.filter { it.haveAllTransactionsBeenRetrieved == false && it.isAccountTypeSupportedByApplication }

    open val showFetchAllTransactionsViewForSelectedAccounts: Boolean
        get() = selectedAccountsForWhichNotAllTransactionsHaveBeenFetched.isNotEmpty()
                && selectedAccountsTransactionRetrievalState == TransactionsRetrievalState.RetrievedTransactions

    open val showStrikingFetchAllTransactionsViewForSelectedAccounts: Boolean
        get() = selectedAccountsForWhichNotAllTransactionsHaveBeenFetched.any { it.doNotShowStrikingFetchAllTransactionsView == false }

    open val selectedAccountsTransactionRetrievalState: TransactionsRetrievalState
        get() = getAccountsTransactionRetrievalState(selectedAccounts)


    open val areAllAccountSelected: Boolean
        get() = selectedAccountType == SelectedAccountType.AllAccounts

    open fun isSingleSelectedBank(bank: TypedBankData): Boolean {
        return selectedAccountType == SelectedAccountType.SingleBank
                && _selectedAccounts.map { it.bank }.toSet().containsExactly(bank)
    }

    open fun isSingleSelectedAccount(account: TypedBankAccount): Boolean {
        return selectedAccountType == SelectedAccountType.SingleAccount
                && _selectedAccounts.containsExactly(account)
    }

    open fun selectedAllAccounts() {
        selectedAccountType = SelectedAccountType.AllAccounts

        setSelectedAccounts(allAccounts)
    }

    open fun selectedBank(bank: TypedBankData) {
        selectedAccountType = SelectedAccountType.SingleBank

        setSelectedAccounts(bank.accounts)
    }

    open fun selectedAccount(account: TypedBankAccount) {
        selectedAccountType = SelectedAccountType.SingleAccount

        setSelectedAccounts(listOf(account))
    }

    protected open fun setSelectedAccounts(accounts: List<TypedBankAccount>) {
        this._selectedAccounts = ArrayList(accounts.filter { it.hideAccount == false }) // make a copy

        callSelectedAccountsChangedListeners(_selectedAccounts)
    }


    open val allBanks: List<TypedBankData>
        get() = bankingClientsForBanks.keys.toList()

    open val allBanksSortedByDisplayIndex: List<TypedBankData>
        get() = allBanks.sortedByDisplayIndex()

    open val allAccounts: List<TypedBankAccount>
        get() = allBanks.flatMap { it.accounts }

    open val allTransactions: List<IAccountTransaction>
        get() = getTransactionsForAccounts(allAccounts)

    open val balanceOfAllAccounts: BigDecimal
        get() = getBalanceForBanks(allBanks)


    open val accountsSupportingRetrievingAccountTransactions: List<TypedBankAccount>
        get() = allAccounts.filter { it.supportsRetrievingAccountTransactions }

    open val hasAccountsSupportingRetrievingTransactions: Boolean
        get() = doAccountsSupportRetrievingTransactions(allAccounts)

    open val doSelectedAccountsSupportRetrievingTransactions: Boolean
        get() = doAccountsSupportRetrievingTransactions(selectedAccounts)

    open fun doAccountsSupportRetrievingTransactions(accounts: List<TypedBankAccount>): Boolean {
        return accounts.firstOrNull { it.supportsRetrievingAccountTransactions } != null
    }


    open val accountsSupportingRetrievingBalance: List<TypedBankAccount>
        get() = allAccounts.filter { it.supportsRetrievingBalance }

    open val hasAccountsSupportingRetrievingBalance: Boolean
        get() = doAccountsSupportRetrievingBalance(allAccounts)

    open val doSelectedAccountsSupportRetrievingBalance: Boolean
        get() = doAccountsSupportRetrievingBalance(selectedAccounts)

    open fun doAccountsSupportRetrievingBalance(accounts: List<TypedBankAccount>): Boolean {
        return accounts.firstOrNull { it.supportsRetrievingBalance || it.balance != BigDecimal.Zero } != null // for credit card account supportsRetrievingBalance may is false but for these balance may gets retrieved otherwise
    }


    open val accountsSupportingTransferringMoney: List<TypedBankAccount>
        get() = allAccounts.filter { it.supportsTransferringMoney }

    open val accountsSupportingTransferringMoneySortedByDisplayIndex: List<TypedBankAccount>
        get() = accountsSupportingTransferringMoney
            .sortedWith(BankAccountComparator())

    open val hasAccountsSupportTransferringMoney: Boolean
        get() = doAccountsSupportTransferringMoney(allAccounts)

    open val doSelectedAccountsSupportTransferringMoney: Boolean
        get() = doAccountsSupportTransferringMoney(selectedAccounts)

    open fun doAccountsSupportTransferringMoney(account: List<TypedBankAccount>): Boolean {
        return account.firstOrNull { it.supportsTransferringMoney } != null
    }


    protected open fun getTransactionsForAccounts(accounts: Collection<TypedBankAccount>): List<IAccountTransaction> {
        return accounts.flatMap { it.bookedTransactions }.sortedByDescending { it.valueDate.millisSinceEpoch } // TODO: someday add unbooked transactions
    }
    open fun currencyIsoCodeOfAccounts(accounts: List<TypedBankAccount>): String {
        // TODO: this is of course not right, it assumes that all accounts have the same currency. But we don't support e.g. calculating the balance of accounts with different currencies anyway
        // at start up list with selectedAccounts is empty
        return accounts.firstOrNull()?.currency ?: currencyInfoProvider.userDefaultCurrencyInfo.isoCode
    }

    protected open fun getAccountsTransactionRetrievalState(accounts: List<TypedBankAccount>): TransactionsRetrievalState {
        val states = accounts.map { getAccountTransactionRetrievalState(it) }

        if (states.contains(TransactionsRetrievalState.RetrievedTransactions)) {
            return TransactionsRetrievalState.RetrievedTransactions
        }

        if (states.contains(TransactionsRetrievalState.NoTransactionsInRetrievedPeriod)) {
            return TransactionsRetrievalState.NoTransactionsInRetrievedPeriod
        }

        if (states.contains(TransactionsRetrievalState.NeverRetrievedTransactions)) {
            return TransactionsRetrievalState.NeverRetrievedTransactions
        }

        if (states.contains(TransactionsRetrievalState.AccountDoesNotSupportFetchingTransactions)) {
            return TransactionsRetrievalState.AccountDoesNotSupportFetchingTransactions
        }

        return TransactionsRetrievalState.AccountTypeNotSupported
    }

    protected open fun getAccountTransactionRetrievalState(account: TypedBankAccount): TransactionsRetrievalState {
        if (account.isAccountTypeSupportedByApplication == false) {
            return TransactionsRetrievalState.AccountTypeNotSupported
        }

        if (account.supportsRetrievingAccountTransactions == false) {
            return TransactionsRetrievalState.AccountDoesNotSupportFetchingTransactions
        }

        if (account.bookedTransactions.isNotEmpty()) {
            return TransactionsRetrievalState.RetrievedTransactions
        }

        if (account.retrievedTransactionsUpTo != null) {
            return TransactionsRetrievalState.NoTransactionsInRetrievedPeriod
        }

        return TransactionsRetrievalState.NeverRetrievedTransactions
    }

    protected open fun getBalanceForBanks(banks: Collection<TypedBankData>): BigDecimal {
        return banks.map { it.balance }.sum()
    }

    protected open fun sumBalance(singleBalances: Collection<BigDecimal>): BigDecimal {
        return singleBalances.sum()
    }


    open fun isFlickerCodeTanMethod(tanMethod: TanMethod): Boolean {
        return tanMethod.type == TanMethodType.ChipTanFlickercode
    }

    open fun isQrTanMethod(tanMethod: TanMethod): Boolean {
        return QrCodeTanMethods.contains(tanMethod.type)
    }

    open fun isPhotoTanMethod(tanMethod: TanMethod): Boolean {
        return PhotoTanMethods.contains(tanMethod.type)
    }

    open fun isOpticalTanMethod(tanMethod: TanMethod): Boolean {
        return OpticalTanMethods.contains(tanMethod.type)
    }

    open fun updateTanMethodSettings(tanMethod: TanMethod, settings: TanMethodSettings?) {
        if (isFlickerCodeTanMethod(tanMethod)) {
            appSettings.flickerCodeSettings = settings
        }
        else if (isQrTanMethod(tanMethod)) {
            appSettings.qrCodeSettings = settings
        }
        else if (isPhotoTanMethod(tanMethod)) {
            appSettings.photoTanSettings = settings
        }

        appSettingsChanged()
    }

    open fun getTanMediaForTanMethod(bank: TypedBankData, tanMethod: TanMethod): List<TanMedium> {
        if (ChipTanTanMethods.contains(tanMethod.type)) {
            return bank.tanMediaSorted.filterIsInstance<TanGeneratorTanMedium>()
        }
        else if (tanMethod.type == TanMethodType.SmsTan) {
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
        asyncRunner.runAsync {
            persistAppSettingsOffUiThread()
        }
    }

    protected open fun persistAppSettingsOffUiThread() {
        try {
            persister.saveOrUpdateAppSettings(appSettings)
        } catch (e: Exception) {
            log.error(e) { "Could not persist AppSettings" }
        }
    }

    protected open fun readAppSettings() {
        try {
            persister.readPersistedAppSettings()?.let {
                appSettings = it
            }
        } catch (e: Exception) {
            log.error(e) { "Could not read AppSettings" }
        }
    }


    open fun addBanksChangedListener(listener: (List<TypedBankData>) -> Unit): Boolean {
        return banksChangedListeners.add(listener)
    }

    open fun removeBanksChangedListener(listener: (List<TypedBankData>) -> Unit): Boolean {
        return banksChangedListeners.remove(listener)
    }

    protected open fun callBanksChangedListeners() {
        val banks = ArrayList(this.allBanks)

        ArrayList(banksChangedListeners).forEach {
            it(banks)
        }
    }


    open fun addRetrievedAccountTransactionsResponseListener(listener: (GetTransactionsResponse) -> Unit): Boolean {
        return retrievedAccountTransactionsResponseListeners.add(listener)
    }

    open fun removeRetrievedAccountTransactionsResponseListener(listener: (GetTransactionsResponse) -> Unit): Boolean {
        return retrievedAccountTransactionsResponseListeners.remove(listener)
    }

    protected open fun callRetrievedAccountTransactionsResponseListener(response: GetTransactionsResponse) {
        ArrayList(retrievedAccountTransactionsResponseListeners).forEach {
            it(response)
        }
    }


    open fun addSelectedAccountsChangedListener(listener: (List<TypedBankAccount>) -> Unit): Boolean {
        return selectedAccountsChangedListeners.add(listener)
    }

    open fun removeSelectedAccountsChangedListener(listener: (List<TypedBankAccount>) -> Unit): Boolean {
        return selectedAccountsChangedListeners.remove(listener)
    }

    protected open fun callSelectedAccountsChangedListeners(selectedAccounts: List<TypedBankAccount>) {
        val accounts = ArrayList(selectedAccounts)

        ArrayList(selectedAccountsChangedListeners).forEach {
            it(accounts)
        }
    }

}
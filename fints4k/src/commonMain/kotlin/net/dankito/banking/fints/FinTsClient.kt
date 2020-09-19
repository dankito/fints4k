package net.dankito.banking.fints

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dankito.banking.fints.callback.FinTsClientCallback
import net.dankito.banking.fints.messages.MessageBuilder
import net.dankito.banking.fints.messages.MessageBuilderResult
import net.dankito.banking.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.banking.fints.messages.datenelemente.implementierte.KundensystemStatusWerte
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.VersionDesSicherheitsverfahrens
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.*
import net.dankito.banking.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.banking.fints.model.*
import net.dankito.banking.fints.response.InstituteSegmentId
import net.dankito.banking.fints.response.Response
import net.dankito.banking.fints.response.ResponseParser
import net.dankito.banking.fints.response.client.*
import net.dankito.banking.fints.response.segments.*
import net.dankito.banking.fints.tan.FlickerCodeDecoder
import net.dankito.banking.fints.tan.TanImageDecoder
import net.dankito.banking.fints.transactions.IAccountTransactionsParser
import net.dankito.banking.fints.transactions.Mt940AccountTransactionsParser
import net.dankito.banking.fints.util.IBase64Service
import net.dankito.banking.fints.util.PureKotlinBase64Service
import net.dankito.utils.multiplatform.log.LoggerFactory
import net.dankito.banking.fints.webclient.IWebClient
import net.dankito.banking.fints.webclient.KtorWebClient
import net.dankito.banking.fints.webclient.WebClientResponse
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.getInnerExceptionMessage


open class FinTsClient(
    protected val callback: FinTsClientCallback,
    protected val webClient: IWebClient = KtorWebClient(),
    protected val base64Service: IBase64Service = PureKotlinBase64Service(),
    protected val messageBuilder: MessageBuilder = MessageBuilder(),
    protected val responseParser: ResponseParser = ResponseParser(),
    protected val mt940Parser: IAccountTransactionsParser = Mt940AccountTransactionsParser(),
    protected val product: ProductData = ProductData("15E53C26816138699C7B6A3E8", "1.0.0") // TODO: get version dynamically
) {

    companion object {
        val FindAccountTransactionsStartRegex = Regex("^HIKAZ:\\d:\\d:\\d\\+@\\d+@", RegexOption.MULTILINE)
        val FindAccountTransactionsEndRegex = Regex("^-'", RegexOption.MULTILINE)

        const val NinetyDaysMillis = 90 * 24 * 60 * 60 * 1000L


        private val log = LoggerFactory.getLogger(FinTsClient::class)
    }


    open var areWeThatGentleToCloseDialogs: Boolean = true

    protected val messageLogField = ArrayList<MessageLogEntry>() // TODO: make thread safe like with CopyOnWriteArrayList

    // in either case remove sensitive data after response is parsed as otherwise some information like account holder name and accounts may is not set yet on BankData
    open val messageLogWithoutSensitiveData: List<MessageLogEntry>
            get() = messageLogField.map { MessageLogEntry(removeSensitiveDataFromMessage(it.message, it.bank), it.time, it.bank) }


    /**
     * Retrieves information about bank (e.g. supported HBCI versions, FinTS server address,
     * supported jobs, ...).
     *
     * On success [bank] parameter is updated afterwards.
     */
    open fun getAnonymousBankInfoAsync(bank: BankData, callback: (FinTsClientResponse) -> Unit) {

        GlobalScope.launch {
            getAnonymousBankInfo(bank, callback)
        }
    }

    /**
     * Retrieves information about bank (e.g. supported HBCI versions, FinTS server address,
     * supported jobs, ...).
     *
     * On success [bank] parameter is updated afterwards.
     */
    open fun getAnonymousBankInfo(bank: BankData, callback: (FinTsClientResponse) -> Unit) {
        val dialogContext = DialogContext(bank, product)

        val message = messageBuilder.createAnonymousDialogInitMessage(dialogContext)

        getAndHandleResponseForMessage(message, dialogContext) { response ->
            if (response.successful) {
                updateBankData(bank, response)

                closeAnonymousDialog(dialogContext, response)
            }

            callback(FinTsClientResponse(response))
        }
    }

    protected open fun closeAnonymousDialog(dialogContext: DialogContext, response: Response) {

        // bank already closed dialog -> there's no need to send dialog end message
        if (areWeThatGentleToCloseDialogs == false || dialogContext.didBankCloseDialog) {
            return
        }

        val dialogEndRequestBody = messageBuilder.createAnonymousDialogEndMessage(dialogContext)

        fireAndForgetMessage(dialogEndRequestBody, dialogContext)
    }


    open fun getUsersTanProcedures(bank: BankData, callback: (AddAccountResponse) -> Unit) {
        // just to ensure settings are in its initial state and that bank sends us bank parameter (BPD),
        // user parameter (UPD) and allowed tan procedures for user (therefore the resetSelectedTanProcedure())
        bank.resetBpdVersion()
        bank.resetUpdVersion()
        /**
         * Sind dem Kundenprodukt die konkreten, für den Benutzer zugelassenen Sicherheitsverfahren nicht bekannt, so können
         * diese über eine Dialoginitialisierung mit Sicherheitsfunktion=999 angefordert werden. Die konkreten Verfahren
         * werden dann über den Rückmeldungscode=3920 zurückgemeldet. Im Rahmen dieses Prozesses darf keine UPD
         * zurückgeliefert werden und die Durchführung anderer Geschäftsvorfälle ist in einem solchen Dialog nicht erlaubt.
         */
        bank.resetSelectedTanProcedure()

        // this is the only case where Einschritt-TAN-Verfahren is accepted: to get user's TAN procedures
        val dialogContext = DialogContext(bank, product, versionOfSecurityProcedure = VersionDesSicherheitsverfahrens.Version_1)

        val message = messageBuilder.createInitDialogMessage(dialogContext)

        getAndHandleResponseForMessage(message, dialogContext) { response ->
            closeDialog(dialogContext)

            handleGetUsersTanProceduresResponse(response, dialogContext, callback)
        }
    }

    protected open fun handleGetUsersTanProceduresResponse(response: Response, dialogContext: DialogContext, callback: (AddAccountResponse) -> Unit) {
        val getUsersTanProceduresResponse = GetUserTanProceduresResponse(response)

        if (getUsersTanProceduresResponse.successful) { // TODO: really update data only on complete successfully response? as it may contain useful information anyway  // TODO: extract method for this code part
            updateBankData(dialogContext.bank, getUsersTanProceduresResponse)
            updateCustomerData(dialogContext.bank, getUsersTanProceduresResponse)
        }

        // even though it is required by specification some banks don't support retrieving user's TAN procedure by setting TAN procedure to '999'
        if (bankDoesNotSupportRetrievingUsersTanProcedures(getUsersTanProceduresResponse)) {
            getBankAndCustomerInfoForNewUserViaAnonymousDialog(dialogContext.bank, callback) // TODO: should not be necessary anymore
        }
        else {
            callback(AddAccountResponse(getUsersTanProceduresResponse, dialogContext.bank))
        }
    }

    protected open fun bankDoesNotSupportRetrievingUsersTanProcedures(response: Response): Boolean {
        return response.successful == false &&
                response.segmentFeedbacks.flatMap { it.feedbacks }.firstOrNull { it.responseCode == 9200 &&
                            it.message == "Gewähltes Zwei-Schritt-Verfahren nicht unterstützt." } != null
    }

    // TODO: this is only a quick fix. Find a better and general solution
    protected open fun getBankAndCustomerInfoForNewUserViaAnonymousDialog(bank: BankData, callback: (AddAccountResponse) -> Unit) {
        getAnonymousBankInfo(bank) { anonymousBankInfoResponse ->
            if (anonymousBankInfoResponse.successful == false) {
                callback(AddAccountResponse(anonymousBankInfoResponse.toResponse(), bank))
            }
            else if (bank.tanProceduresSupportedByBank.isEmpty()) { // should only be a theoretical error
                callback(AddAccountResponse(Response(true,
                    errorMessage = "Die TAN Verfahren der Bank konnten nicht ermittelt werden"), bank)) // TODO: translate
            }
            else {
                bank.tanProceduresAvailableForUser = bank.tanProceduresSupportedByBank
                getUsersTanProcedure(bank)

                val dialogContext = DialogContext(bank, product)

                initDialogAfterSuccessfulChecks(dialogContext) { initDialogResponse ->
                    closeDialog(dialogContext)

                    callback(AddAccountResponse(initDialogResponse, bank))
                }
            }
        }
    }


    protected open fun getAccounts(bank: BankData, callback: (AddAccountResponse) -> Unit) {

        val dialogContext = DialogContext(bank, product)

        initDialogAfterSuccessfulChecks(dialogContext) { response ->
            closeDialog(dialogContext)

            if (response.successful) {
                updateBankData(bank, response)
                updateCustomerData(bank, response)
            }

            callback(AddAccountResponse(response, bank))
        }
    }


    /**
     * According to specification synchronizing customer system id is required:
     * "Die Kundensystem-ID ist beim HBCI RAH- / RDH- sowie dem PIN/TAN-Verfahren erforderlich."
     *
     * But as tests show this can be omitted.
     *
     * But when you do it, this has to be done in an extra dialog as dialog has to be initialized
     * with retrieved customer system id.
     *
     * If you change customer system id during a dialog your messages get rejected by bank institute.
     */
    protected open fun synchronizeCustomerSystemId(bank: BankData, callback: (FinTsClientResponse) -> Unit) {

        val dialogContext = DialogContext(bank, product)
        val message = messageBuilder.createSynchronizeCustomerSystemIdMessage(dialogContext)

        getAndHandleResponseForMessage(message, dialogContext) { response ->
            if (response.successful) {
                updateBankData(bank, response)
                updateCustomerData(bank, response)

                closeDialog(dialogContext)
            }

            callback(FinTsClientResponse(response))
        }
    }


    open fun addAccountAsync(bank: BankData, callback: (AddAccountResponse) -> Unit) {

        val originalAreWeThatGentleToCloseDialogs = areWeThatGentleToCloseDialogs
        areWeThatGentleToCloseDialogs = false

        /*      First dialog: Get user's basic data like BPD, customer system ID and her TAN procedures     */

        getUsersTanProcedures(bank) { newUserInfoResponse ->

            if (newUserInfoResponse.successful == false) { // bank parameter (FinTS server address, ...) already seem to be wrong
                callback(newUserInfoResponse)
                return@getUsersTanProcedures
            }


            // do not ask user for tan at this stage
            var didOverwriteUserUnselectedTanProcedure = false
            if (bank.isTanProcedureSelected == false && bank.tanProceduresAvailableForUser.isNotEmpty()) {

                if (bank.tanProceduresAvailableForUser.size == 1) { // user has only one TAN procedure -> set it and we're done
                    bank.selectedTanProcedure = bank.tanProceduresAvailableForUser.first()
                }
                else {
                    didOverwriteUserUnselectedTanProcedure = true
                    bank.selectedTanProcedure = selectSuggestedTanProcedure(bank) ?: bank.tanProceduresAvailableForUser.first()
                }
            }


            /*      Second dialgo: some banks require that in order to initialize a dialog with strong customer authorization TAN media is required       */

            getTanMediaList(bank, TanMedienArtVersion.Alle, TanMediumKlasse.AlleMedien) {

                /*      Third dialog: Now we can initialize our first dialog with strong customer authorization. Use it to get UPD and customer's accounts        */

                getAccounts(bank) { getAccountsResponse ->

                    if (getAccountsResponse.successful == false) {
                        callback(getAccountsResponse)
                        return@getAccounts
                    }

                    /*      Fourth dialog: Try to retrieve account balances and transactions of last 90 days without TAN     */

                    addAccountGetAccountBalancesAndTransactions(bank, newUserInfoResponse, didOverwriteUserUnselectedTanProcedure,
                        originalAreWeThatGentleToCloseDialogs, callback)
                }
            }
        }
    }

    protected open fun addAccountGetAccountBalancesAndTransactions(bank: BankData, newUserInfoResponse: AddAccountResponse,
                                                                   didOverwriteUserUnselectedTanProcedure: Boolean, originalAreWeThatGentleToCloseDialogs: Boolean,
                                                                   callback: (AddAccountResponse) -> Unit) {
        // TODO: or add a default RetrievedAccountData instance for each account?
        val retrievedAccountData = mutableListOf<RetrievedAccountData>()

        val accountSupportingRetrievingTransactions = bank.accounts.filter { it.supportsFeature(AccountFeature.RetrieveBalance) || it.supportsFeature(AccountFeature.RetrieveAccountTransactions) }
        val countAccountSupportingRetrievingTransactions = accountSupportingRetrievingTransactions.size
        var countRetrievedAccounts = 0

        if (countAccountSupportingRetrievingTransactions == 0) {
            addAccountAfterRetrievingTransactions(bank, newUserInfoResponse, didOverwriteUserUnselectedTanProcedure,
                originalAreWeThatGentleToCloseDialogs, retrievedAccountData, callback)
        }

        accountSupportingRetrievingTransactions.forEach { account ->
            tryGetTransactionsOfLast90DaysWithoutTan(bank, account) { response ->
                retrievedAccountData.addAll(response.retrievedData)

                countRetrievedAccounts++
                if (countRetrievedAccounts == countAccountSupportingRetrievingTransactions) {
                    addAccountAfterRetrievingTransactions(bank, newUserInfoResponse, didOverwriteUserUnselectedTanProcedure, originalAreWeThatGentleToCloseDialogs,
                        retrievedAccountData, callback)
                }
            }
        }
    }

    protected open fun addAccountAfterRetrievingTransactions(bank: BankData, newUserInfoResponse: AddAccountResponse,
                                                             didOverwriteUserUnselectedTanProcedure: Boolean, originalAreWeThatGentleToCloseDialogs: Boolean,
                                                             retrievedAccountData: List<RetrievedAccountData>,
                                                             callback: (AddAccountResponse) -> Unit) {
        if (didOverwriteUserUnselectedTanProcedure) {
            bank.resetSelectedTanProcedure()
        }

        areWeThatGentleToCloseDialogs = originalAreWeThatGentleToCloseDialogs

        callback(AddAccountResponse(newUserInfoResponse.toResponse(), bank, retrievedAccountData))
    }


    /**
     * Some banks support that according to PSD2 account transactions may be retrieved without
     * a TAN (= no strong customer authorization needed).
     *
     * Check if bank supports this.
     */
    open fun tryGetTransactionsOfLast90DaysWithoutTan(bank: BankData, account: AccountData, callback: (GetTransactionsResponse) -> Unit) {

        val now = Date()
        val ninetyDaysAgo = Date(now.millisSinceEpoch - NinetyDaysMillis)

        getTransactionsAsync(GetTransactionsParameter(account, account.supportsFeature(AccountFeature.RetrieveBalance), ninetyDaysAgo, abortIfTanIsRequired = true), bank) { response ->
            callback(response)
        }
    }

    open fun getTransactionsAsync(parameter: GetTransactionsParameter, bank: BankData, callback: (GetTransactionsResponse) -> Unit) {

        val dialogContext = DialogContext(bank, product)

        initDialog(dialogContext) { initDialogResponse ->

            if (initDialogResponse.successful == false) {
                callback(GetTransactionsResponse(initDialogResponse, RetrievedAccountData.unsuccessfulList(parameter.account)))
            }
            else {
                mayGetBalance(parameter, dialogContext) { balanceResponse ->
                    if (balanceResponse.successful == false && balanceResponse.couldCreateMessage == true) { // don't break here if required HKSAL message is not implemented
                        closeDialog(dialogContext)
                        callback(GetTransactionsResponse(balanceResponse, RetrievedAccountData.unsuccessfulList(parameter.account)))
                    }
                    else {
                        getTransactionsAfterInitAndGetBalance(parameter, dialogContext, balanceResponse, callback)
                    }
                }
            }
        }
    }

    protected open fun getTransactionsAfterInitAndGetBalance(parameter: GetTransactionsParameter, dialogContext: DialogContext,
                                                             balanceResponse: Response, callback: (GetTransactionsResponse) -> Unit) {
        val balance: Money? = balanceResponse.getFirstSegmentById<BalanceSegment>(InstituteSegmentId.Balance)?.let {
                Money(it.balance, it.currency)
            }
        val bookedTransactions = mutableSetOf<AccountTransaction>()
        val unbookedTransactions = mutableSetOf<Any>()

        val message = messageBuilder.createGetTransactionsMessage(parameter, dialogContext)

        var remainingMt940String = ""

        dialogContext.abortIfTanIsRequired = parameter.abortIfTanIsRequired

        dialogContext.chunkedResponseHandler = { response ->
            response.getFirstSegmentById<ReceivedAccountTransactions>(InstituteSegmentId.AccountTransactionsMt940)?.let { transactionsSegment ->
                val (chunkTransaction, remainder) = mt940Parser.parseTransactionsChunk(remainingMt940String + transactionsSegment.bookedTransactionsString, parameter.account)

                bookedTransactions.addAll(chunkTransaction)
                remainingMt940String = remainder

                parameter.retrievedChunkListener?.invoke(bookedTransactions)
            }
        }

        getAndHandleResponseForMessage(message, dialogContext) { response ->
            closeDialog(dialogContext)

            val successful = response.successful && (parameter.alsoRetrieveBalance == false || balance != null)

            callback(GetTransactionsResponse(response, listOf(RetrievedAccountData(parameter.account, successful, balance, bookedTransactions, unbookedTransactions)),
                if (parameter.maxCountEntries != null) parameter.isSettingMaxCountEntriesAllowedByBank else null
            ))
        }
    }

    protected open fun mayGetBalance(parameter: GetTransactionsParameter, dialogContext: DialogContext, callback: (Response) -> Unit) {
        if (parameter.alsoRetrieveBalance && parameter.account.supportsFeature(AccountFeature.RetrieveBalance)) {
            val message = messageBuilder.createGetBalanceMessage(parameter.account, dialogContext)

            getAndHandleResponseForMessage(message, dialogContext) { response ->
                callback(response)
            }
        }
    }


    open fun getTanMediaListAsync(bank: BankData, tanMediaKind: TanMedienArtVersion = TanMedienArtVersion.Alle,
                                  tanMediumClass: TanMediumKlasse = TanMediumKlasse.AlleMedien,
                                  callback: (GetTanMediaListResponse) -> Unit) {

        GlobalScope.launch {
            getTanMediaList(bank, tanMediaKind, tanMediumClass, callback)
        }
    }

    open fun getTanMediaList(bank: BankData, tanMediaKind: TanMedienArtVersion = TanMedienArtVersion.Alle,
                             tanMediumClass: TanMediumKlasse = TanMediumKlasse.AlleMedien, callback: (GetTanMediaListResponse) -> Unit) {

        sendMessageAndHandleResponse(bank, true, CustomerSegmentId.TanMediaList, { dialogContext ->
            messageBuilder.createGetTanMediaListMessage(dialogContext, tanMediaKind, tanMediumClass)
        }) { response ->
            handleGetTanMediaListResponse(response, bank, callback)
        }
    }

    private fun handleGetTanMediaListResponse(response: Response, bank: BankData, callback: (GetTanMediaListResponse) -> Unit) {
        // TAN media list (= TAN generator list) is only returned for users with chipTAN TAN procedures
        val tanMediaList = if (response.successful == false) null
        else response.getFirstSegmentById<TanMediaList>(InstituteSegmentId.TanMediaList)

        tanMediaList?.let {
            bank.tanMedia = it.tanMedia
        }

        callback(GetTanMediaListResponse(response, tanMediaList))
    }


    open fun changeTanMedium(newActiveTanMedium: TanGeneratorTanMedium, bank: BankData, callback: (FinTsClientResponse) -> Unit) {

        if (bank.changeTanMediumParameters?.enteringAtcAndTanRequired == true) {
            this.callback.enterTanGeneratorAtc(bank, newActiveTanMedium) { enteredAtc ->
                if (enteredAtc.hasAtcBeenEntered == false) {
                    val message = "Bank requires to enter ATC and TAN in order to change TAN medium." // TODO: translate
                    callback(FinTsClientResponse(Response(false, errorMessage = message)))
                }
                else {
                    sendChangeTanMediumMessage(bank, newActiveTanMedium, enteredAtc, callback)
                }
            }
        }
        else {
            sendChangeTanMediumMessage(bank, newActiveTanMedium, null, callback)
        }
    }

    protected open fun sendChangeTanMediumMessage(bank: BankData, newActiveTanMedium: TanGeneratorTanMedium, enteredAtc: EnterTanGeneratorAtcResult?,
                                                  callback: (FinTsClientResponse) -> Unit) {

        sendMessageAndHandleResponse(bank, false, null, { dialogContext ->
            messageBuilder.createChangeTanMediumMessage(newActiveTanMedium, dialogContext, enteredAtc?.tan, enteredAtc?.atc)
        }) { response ->
            callback(FinTsClientResponse(response))
        }
    }


    open fun doBankTransferAsync(bankTransferData: BankTransferData, bank: BankData, account: AccountData, callback: (FinTsClientResponse) -> Unit) {

        sendMessageAndHandleResponse(bank, true, null, { dialogContext ->
            messageBuilder.createBankTransferMessage(bankTransferData, account, dialogContext)
        }) { response ->
            callback(FinTsClientResponse(response))
        }
    }


    protected open fun sendMessageAndHandleResponse(bank: BankData, messageMayRequiresTan: Boolean = true, segmentForNonStrongCustomerAuthenticationTwoStepTanProcess: CustomerSegmentId? = null,
                                                    createMessage: (DialogContext) -> MessageBuilderResult, callback: (Response) -> Unit) {

        val dialogContext = DialogContext(bank, product)

        if (segmentForNonStrongCustomerAuthenticationTwoStepTanProcess == null) {
            initDialog(dialogContext) { initDialogResponse ->
                sendMessageAndHandleResponseAfterDialogInitialization(dialogContext, initDialogResponse, createMessage, callback)
            }
        }
        else {
            initInitDialogMessageWithoutStrongCustomerAuthenticationAfterSuccessfulChecks(dialogContext, segmentForNonStrongCustomerAuthenticationTwoStepTanProcess) { initDialogResponse ->
                sendMessageAndHandleResponseAfterDialogInitialization(dialogContext, initDialogResponse, createMessage, callback)
            }
        }
    }

    private fun sendMessageAndHandleResponseAfterDialogInitialization(dialogContext: DialogContext, initDialogResponse: Response, createMessage: (DialogContext) -> MessageBuilderResult, callback: (Response) -> Unit) {

        if (initDialogResponse.successful == false) {
            callback(initDialogResponse)
        }
        else {
            val message = createMessage(dialogContext)

            getAndHandleResponseForMessage(message, dialogContext) { response ->
                closeDialog(dialogContext)

                callback(response)
            }
        }
    }

    protected open fun initDialog(dialogContext: DialogContext, callback: (Response) -> Unit) {

        // we first need to retrieve supported tan procedures and jobs before we can do anything
        ensureBasicBankDataRetrieved(dialogContext.bank) { retrieveBasicBankDataResponse ->
            if (retrieveBasicBankDataResponse.successful == false) {
                callback(retrieveBasicBankDataResponse)
            }
            else {
                // as in the next step we have to supply user's tan procedure, ensure user selected his or her
                ensureTanProcedureIsSelected(dialogContext.bank) { tanProcedureSelectedResponse ->
                    if (tanProcedureSelectedResponse.successful == false) {
                        callback(tanProcedureSelectedResponse)
                    }
                    else {
                        initDialogAfterSuccessfulChecks(dialogContext, callback)
                    }
                }
            }
        }
    }

    protected open fun initDialogAfterSuccessfulChecks(dialogContext: DialogContext, callback: (Response) -> Unit) {

        val message = messageBuilder.createInitDialogMessage(dialogContext)

        getAndHandleResponseForMessage(message, dialogContext) { response ->

            if (response.successful) {
                updateBankData(dialogContext.bank, response)
                updateCustomerData(dialogContext.bank, response)
            }

            callback(response)
        }
    }

    protected open fun initInitDialogMessageWithoutStrongCustomerAuthenticationAfterSuccessfulChecks(dialogContext: DialogContext, segmentIdForTwoStepTanProcess: CustomerSegmentId?,
                                                                                                     callback: (Response) -> Unit) {

        val message = messageBuilder.createInitDialogMessageWithoutStrongCustomerAuthentication(dialogContext, segmentIdForTwoStepTanProcess)

        getAndHandleResponseForMessage(message, dialogContext) { response ->
            if (response.successful) {
                updateBankData(dialogContext.bank, response)
                updateCustomerData(dialogContext.bank, response)
            }

            callback(response)
        }
    }

    protected open fun closeDialog(dialogContext: DialogContext) {

        // bank already closed dialog -> there's no need to send dialog end message
        if (areWeThatGentleToCloseDialogs == false || dialogContext.didBankCloseDialog) {
            return
        }

        val dialogEndRequestBody = messageBuilder.createDialogEndMessage(dialogContext)

        fireAndForgetMessage(dialogEndRequestBody, dialogContext)
    }


    protected open fun ensureBasicBankDataRetrieved(bank: BankData, callback: (Response) -> Unit) {
        if (bank.tanProceduresSupportedByBank.isEmpty() || bank.supportedJobs.isEmpty()) {
            getUsersTanProcedures(bank) { getBankInfoResponse ->
                if (getBankInfoResponse.successful == false || bank.tanProceduresSupportedByBank.isEmpty()
                    || bank.supportedJobs.isEmpty()) {

                    callback(Response(false, errorMessage =
                    "Could not retrieve basic bank data like supported tan procedures or supported jobs")) // TODO: translate // TODO: add as messageToShowToUser
                }
                else {
                    callback(Response(true))
                }
            }
        }
        else {
            callback(Response(true))
        }
    }

    protected open fun ensureTanProcedureIsSelected(bank: BankData, callback: (Response) -> Unit) {
        if (bank.isTanProcedureSelected == false) {
            if (bank.tanProceduresAvailableForUser.isEmpty()) {
                getUsersTanProcedures(bank) {
                    if (bank.tanProceduresAvailableForUser.isEmpty()) { // could not retrieve supported tan procedures for user
                        callback(Response(false, noTanProcedureSelected = true))
                    }
                    else {
                        getUsersTanProcedure(bank)
                        callback(Response(bank.isTanProcedureSelected, noTanProcedureSelected = !!!bank.isTanProcedureSelected))
                    }
                }
            }
            else {
                getUsersTanProcedure(bank)
                callback(Response(bank.isTanProcedureSelected, noTanProcedureSelected = !!!bank.isTanProcedureSelected))
            }
        }
        else {
            callback(Response(bank.isTanProcedureSelected, noTanProcedureSelected = !!!bank.isTanProcedureSelected))
        }
    }

    protected open fun getUsersTanProcedure(bank: BankData) {
        if (bank.tanProceduresAvailableForUser.size == 1) { // user has only one TAN procedure -> set it and we're done
            bank.selectedTanProcedure = bank.tanProceduresAvailableForUser.first()
        }
        else {
            // we know user's supported tan procedures, now ask user which one to select
            callback.askUserForTanProcedure(bank.tanProceduresAvailableForUser, selectSuggestedTanProcedure(bank)) { selectedTanProcedure ->
                selectedTanProcedure?.let {
                    bank.selectedTanProcedure = selectedTanProcedure
                }
            }
        }
    }

    protected open fun selectSuggestedTanProcedure(bank: BankData): TanProcedure? {
        return bank.tanProceduresAvailableForUser.firstOrNull { it.type != TanProcedureType.ChipTanUsb && it.type != TanProcedureType.SmsTan && it.type != TanProcedureType.ChipTanManuell }
            ?: bank.tanProceduresAvailableForUser.firstOrNull { it.type != TanProcedureType.ChipTanUsb && it.type != TanProcedureType.SmsTan }
            ?: bank.tanProceduresAvailableForUser.firstOrNull { it.type != TanProcedureType.ChipTanUsb }
            ?: bank.tanProceduresAvailableForUser.firstOrNull()
    }


    protected open fun getAndHandleResponseForMessage(message: MessageBuilderResult, dialogContext: DialogContext, callback: (Response) -> Unit) {
        if (message.createdMessage == null) {
            callback(Response(false, messageCreationError = message))
        }
        else {
            getAndHandleResponseForMessage(message.createdMessage, dialogContext) { response ->
                handleMayRequiresTan(response, dialogContext) { handledResponse ->
                    // if there's a Aufsetzpunkt (continuationId) set, then response is not complete yet, there's more information to fetch by sending this Aufsetzpunkt
                    handledResponse.aufsetzpunkt?.let { continuationId ->
                        if (handledResponse.followUpResponse == null) { // for re-sent messages followUpResponse is already set and dialog already closed -> would be overwritten with an error response that dialog is closed
                            if (message.isSendEnteredTanMessage() == false) { // for sending TAN no follow up message can be created -> filter out, otherwise chunkedResponseHandler would get called twice for same response
                                dialogContext.chunkedResponseHandler?.invoke(handledResponse)
                            }

                            getFollowUpMessageForContinuationId(handledResponse, continuationId, message, dialogContext) { followUpResponse ->
                                handledResponse.followUpResponse = followUpResponse
                                handledResponse.hasFollowUpMessageButCouldNotReceiveIt = handledResponse.followUpResponse == null

                                callback(handledResponse)
                            }
                        }
                        else {
                            callback(handledResponse)
                        }
                    }
                    ?: run {
                        // e.g. response = enter TAN response, but handledResponse is then response after entering TAN, e.g. account transactions
                        // -> chunkedResponseHandler would get called for same handledResponse multiple times
                        if (response == handledResponse) {
                            dialogContext.chunkedResponseHandler?.invoke(handledResponse)
                        }

                        callback(handledResponse)
                    }
                }
            }
        }
    }

    protected open fun getAndHandleResponseForMessage(requestBody: String, dialogContext: DialogContext, callback: (Response) -> Unit) {
        addMessageLog(requestBody, MessageLogEntryType.Sent, dialogContext)

        getResponseForMessage(requestBody, dialogContext.bank.finTs3ServerAddress) { webResponse ->
            val response = handleResponse(webResponse, dialogContext)

            dialogContext.response = response

            response.messageHeader?.let { header -> dialogContext.dialogId = header.dialogId }
            dialogContext.didBankCloseDialog = response.didBankCloseDialog

            callback(response)
        }
    }

    protected open fun getResponseForMessage(requestBody: String, finTs3ServerAddress: String, callback: (WebClientResponse) -> Unit) {
        val encodedRequestBody = base64Service.encode(requestBody)

        webClient.post(finTs3ServerAddress, encodedRequestBody, "application/octet-stream", IWebClient.DefaultUserAgent, callback)
    }

    protected open fun fireAndForgetMessage(message: MessageBuilderResult, dialogContext: DialogContext) {
        message.createdMessage?.let { requestBody ->
            addMessageLog(requestBody, MessageLogEntryType.Sent, dialogContext)

            getResponseForMessage(requestBody, dialogContext.bank.finTs3ServerAddress) { }

            // if really needed add received response to message log here
        }
    }

    protected open fun handleResponse(webResponse: WebClientResponse, dialogContext: DialogContext): Response {
        val responseBody = webResponse.body

        if (webResponse.successful && responseBody != null) {

            try {
                val decodedResponse = decodeBase64Response(responseBody)

                addMessageLog(decodedResponse, MessageLogEntryType.Received, dialogContext)

                return responseParser.parse(decodedResponse)
            } catch (e: Exception) {
                log.error(e) { "Could not decode responseBody:\r\n'$responseBody'" }

                return Response(false, errorMessage = e.getInnerExceptionMessage())
            }
        }
        else {
            val bank = dialogContext.bank
            log.error(webResponse.error) { "Request to $bank (${bank.finTs3ServerAddress}) failed" }
        }

        return Response(false, errorMessage = webResponse.error?.getInnerExceptionMessage())
    }

    protected open fun decodeBase64Response(responseBody: String): String {
        return base64Service.decode(responseBody.replace("\r", "").replace("\n", ""))
    }


    protected open fun getFollowUpMessageForContinuationId(response: Response, continuationId: String, message: MessageBuilderResult,
                                                           dialogContext: DialogContext, callback: (Response?) -> Unit) {

        messageBuilder.rebuildMessageWithContinuationId(message, continuationId, dialogContext)?.let { followUpMessage ->
            getAndHandleResponseForMessage(followUpMessage, dialogContext, callback)
        }
        ?: run { callback(null) }
    }


    protected open fun addMessageLog(message: String, type: MessageLogEntryType, dialogContext: DialogContext) {
        val timeStamp = Date()
        val messagePrefix = "${if (type == MessageLogEntryType.Sent) "Sending" else "Received"} message:\r\n" // currently no need to translate
        val prettyPrintMessage = prettyPrintHbciMessage(message)
        val prettyPrintMessageWithPrefix = "$messagePrefix$prettyPrintMessage"

        log.debug { prettyPrintMessageWithPrefix }

        messageLogField.add(MessageLogEntry(prettyPrintMessageWithPrefix, timeStamp, dialogContext.bank))
    }

    protected fun prettyPrintHbciMessage(message: String): String {
        return message.replace("'", "'\r\n")
    }

    protected open fun removeSensitiveDataFromMessage(message: String, bank: BankData): String {
        var prettyPrintMessageWithoutSensitiveData = message
            .replace(bank.customerId, "<customer_id>")
            .replace("+" + bank.pin, "+<pin>")

        if (bank.customerName.isNotBlank()) {
            prettyPrintMessageWithoutSensitiveData = prettyPrintMessageWithoutSensitiveData
                .replace(bank.customerName, "<customer_name>", true)
        }

        bank.accounts.forEach { account ->
            prettyPrintMessageWithoutSensitiveData = prettyPrintMessageWithoutSensitiveData
                .replace(account.accountIdentifier, "<account_identifier>")

            if (account.accountHolderName.isNotBlank()) {
                prettyPrintMessageWithoutSensitiveData = prettyPrintMessageWithoutSensitiveData
                    .replace(account.accountHolderName, "<account_holder>", true)
            }
        }

        return removeAccountTransactions(prettyPrintMessageWithoutSensitiveData)
    }

    protected open fun removeAccountTransactions(message: String): String {
        FindAccountTransactionsStartRegex.find(message)?.let { startMatchResult ->
            FindAccountTransactionsEndRegex.find(message, startMatchResult.range.endInclusive)?.let { endMatchResult ->
                return message.replaceRange(IntRange(startMatchResult.range.endInclusive, endMatchResult.range.start), "<account_transactions>")
            }
        }

        return message
    }


    protected open fun handleMayRequiresTan(response: Response, dialogContext: DialogContext, callback: (Response) -> Unit) { // TODO: use response from DialogContext

        if (response.isStrongAuthenticationRequired) {
            if (dialogContext.abortIfTanIsRequired) {
                response.tanRequiredButWeWereToldToAbortIfSo = true

                callback(response)
                return
            }
            else if (response.tanResponse != null) {
                response.tanResponse?.let { tanResponse ->
                    handleEnteringTanRequired(tanResponse, response, dialogContext, callback)
                }

                return
            }
        }

        // TODO: check if response contains '3931 TAN-Generator gesperrt, Synchronisierung erforderlich' or
        //  '3933 TAN-Generator gesperrt, Synchronisierung erforderlich Kartennummer ##########' message,
        //  call callback.enterAtc() and implement and call HKTSY job  (p. 77)

        // TODO: also check '9931 Sperrung des Kontos nach %1 Fehlversuchen' -> if %1 == 3 synchronize TAN generator
        //  as it's quite unrealistic that user entered TAN wrong three times, in most cases TAN generator is not synchronized

        callback(response)
    }

    protected open fun handleEnteringTanRequired(tanResponse: TanResponse, response: Response, dialogContext: DialogContext, callback: (Response) -> Unit) {
        val bank = dialogContext.bank // TODO: copy required data to TanChallenge
        val tanChallenge = createTanChallenge(tanResponse, bank)

        this.callback.enterTan(bank, tanChallenge)  { enteredTanResult ->
            handleEnterTanResult(enteredTanResult, tanResponse, response, dialogContext, callback)
        }
    }

    protected open fun createTanChallenge(tanResponse: TanResponse, bank: BankData): TanChallenge {
        // TODO: is this true for all tan procedures?
        val messageToShowToUser = tanResponse.challenge ?: ""
        val challenge = tanResponse.challengeHHD_UC ?: ""
        val tanProcedure = bank.selectedTanProcedure

        return when (tanProcedure.type) {
            TanProcedureType.ChipTanFlickercode ->
                FlickerCodeTanChallenge(FlickerCodeDecoder().decodeChallenge(challenge, tanProcedure.hhdVersion ?: HHDVersion.HHD_1_4), // HHD 1.4 is currently the most used version
                    messageToShowToUser, challenge, tanProcedure, tanResponse.tanMediaIdentifier)

            TanProcedureType.ChipTanQrCode, TanProcedureType.ChipTanPhotoTanMatrixCode,
            TanProcedureType.QrCode, TanProcedureType.photoTan ->
                ImageTanChallenge(TanImageDecoder().decodeChallenge(challenge), messageToShowToUser, challenge, tanProcedure, tanResponse.tanMediaIdentifier)

            else -> TanChallenge(messageToShowToUser, challenge, tanProcedure, tanResponse.tanMediaIdentifier)
        }
    }

    protected open fun handleEnterTanResult(enteredTanResult: EnterTanResult, tanResponse: TanResponse, response: Response,
                                            dialogContext: DialogContext, callback: (Response) -> Unit) {

        if (enteredTanResult.changeTanProcedureTo != null) {
            handleUserAsksToChangeTanProcedureAndResendLastMessage(enteredTanResult.changeTanProcedureTo, dialogContext, callback)
        }
        else if (enteredTanResult.changeTanMediumTo is TanGeneratorTanMedium) {
            handleUserAsksToChangeTanMediumAndResendLastMessage(enteredTanResult.changeTanMediumTo, dialogContext,
                enteredTanResult.changeTanMediumResultCallback, callback)
        }
        else if (enteredTanResult.enteredTan == null) {
            // i tried to send a HKTAN with cancelJob = true but then i saw there are no tan procedures that support cancellation (at least not at my bank)
            // but it's not required anyway, tan times out after some time. Simply don't respond anything and close dialog
            response.tanRequiredButUserDidNotEnterOne = true

            callback(response)
        }
        else {
            sendTanToBank(enteredTanResult.enteredTan, tanResponse, dialogContext, callback)
        }
    }

    protected open fun sendTanToBank(enteredTan: String, tanResponse: TanResponse, dialogContext: DialogContext, callback: (Response) -> Unit) {

        val message = messageBuilder.createSendEnteredTanMessage(enteredTan, tanResponse, dialogContext)

        getAndHandleResponseForMessage(message, dialogContext, callback)
    }

    protected open fun handleUserAsksToChangeTanProcedureAndResendLastMessage(changeTanProcedureTo: TanProcedure, dialogContext: DialogContext, callback: (Response) -> Unit) {

        dialogContext.bank.selectedTanProcedure = changeTanProcedureTo


        val lastCreatedMessage = dialogContext.currentMessage

        lastCreatedMessage?.let { closeDialog(dialogContext) }

        resendMessageInNewDialog(lastCreatedMessage, dialogContext, callback)
    }

    protected open fun handleUserAsksToChangeTanMediumAndResendLastMessage(changeTanMediumTo: TanGeneratorTanMedium,
                                                                           dialogContext: DialogContext,
                                                                           changeTanMediumResultCallback: ((FinTsClientResponse) -> Unit)?,
                                                                           callback: (Response) -> Unit) {

        val lastCreatedMessage = dialogContext.currentMessage

        lastCreatedMessage?.let { closeDialog(dialogContext) }


        changeTanMedium(changeTanMediumTo, dialogContext.bank) { changeTanMediumResponse ->
            changeTanMediumResultCallback?.invoke(changeTanMediumResponse)

            if (changeTanMediumResponse.successful == false || lastCreatedMessage == null) {
                callback(changeTanMediumResponse.toResponse())
            }
            else {
                resendMessageInNewDialog(lastCreatedMessage, dialogContext, callback)
            }
        }
    }


    protected open fun resendMessageInNewDialog(lastCreatedMessage: MessageBuilderResult?, previousDialogContext: DialogContext, callback: (Response) -> Unit) {

        if (lastCreatedMessage != null) { // do not use previousDialogContext.currentMessage as this may is previous dialog's dialog close message
            val newDialogContext = DialogContext(previousDialogContext.bank, previousDialogContext.product, chunkedResponseHandler = previousDialogContext.chunkedResponseHandler)

            initDialog(newDialogContext) { initDialogResponse ->
                if (initDialogResponse.successful == false) {
                    callback(initDialogResponse)
                }
                else {
                    val newMessage = messageBuilder.rebuildMessage(lastCreatedMessage, newDialogContext)

                    getAndHandleResponseForMessage(newMessage, newDialogContext) { response ->
                        closeDialog(newDialogContext)

                        callback(response)
                    }
                }
            }
        }
        else {
            val errorMessage = "There's no last action (like retrieve account transactions, transfer money, ...) to re-send with new TAN procedure. Probably an internal programming error." // TODO: translate
            callback(Response(false, errorMessage = errorMessage)) // should never come to this
        }
    }


    protected open fun updateBankData(bank: BankData, response: Response) {
        response.getFirstSegmentById<BankParameters>(InstituteSegmentId.BankParameters)?.let { bankParameters ->
            bank.bpdVersion = bankParameters.bpdVersion
            bank.bankName = adjustBankName(bankParameters.bankName)
            bank.bankCode = bankParameters.bankCode
            bank.countryCode = bankParameters.bankCountryCode
            bank.countMaxJobsPerMessage = bankParameters.countMaxJobsPerMessage
            bank.supportedHbciVersions = bankParameters.supportedHbciVersions
            bank.supportedLanguages = bankParameters.supportedLanguages

//            bank.bic = bankParameters. // TODO: where's the BIC?
        }

        response.getFirstSegmentById<PinInfo>(InstituteSegmentId.PinInfo)?.let { pinInfo ->
            bank.pinInfo = pinInfo
        }

        response.getFirstSegmentById<TanInfo>(InstituteSegmentId.TanInfo)?.let { tanInfo ->
            bank.tanProceduresSupportedByBank = mapToTanProcedures(tanInfo)
        }

        response.getFirstSegmentById<CommunicationInfo>(InstituteSegmentId.CommunicationInfo)?.let { communicationInfo ->
            communicationInfo.parameters.firstOrNull { it.type == Kommunikationsdienst.Https }?.address?.let { address ->
                bank.finTs3ServerAddress = if (address.startsWith("https://", true)) address else "https://$address"
            }
        }

        response.getFirstSegmentById<SepaAccountInfo>(InstituteSegmentId.SepaAccountInfo)?.let { sepaAccountInfo ->
            sepaAccountInfo.account.bic?.let {
                bank.bic = it // TODO: really set BIC on bank then?
            }
        }

        response.getFirstSegmentById<ChangeTanMediaParameters>(InstituteSegmentId.ChangeTanMediaParameters)?.let { parameters ->
            bank.changeTanMediumParameters = parameters
        }

        if (response.supportedJobs.isNotEmpty()) {
            bank.supportedJobs = response.supportedJobs
        }
    }

    protected open fun adjustBankName(bankName: String): String {
        return bankName.replace("DB24-Filiale", "Deutsche Bank") // set a better name for Deutsche Bank's self title 'DB24-Filiale'
    }

    protected open fun updateCustomerData(bank: BankData, response: Response) {
        response.getFirstSegmentById<BankParameters>(InstituteSegmentId.BankParameters)?.let { bankParameters ->
            // TODO: ask user if there is more than one supported language? But it seems that almost all banks only support German.
            if (bank.selectedLanguage == Dialogsprache.Default && bankParameters.supportedLanguages.isNotEmpty()) {
                bank.selectedLanguage = bankParameters.supportedLanguages.first()
            }
        }

        response.getFirstSegmentById<ReceivedSynchronization>(InstituteSegmentId.Synchronization)?.let { synchronization ->
            synchronization.customerSystemId?.let {
                bank.customerSystemId = it

                bank.customerSystemStatus = KundensystemStatusWerte.Benoetigt // TODO: didn't find out for sure yet, but i think i read somewhere, that this has to be set when customerSystemId is set
            }
        }

        response.getSegmentsById<AccountInfo>(InstituteSegmentId.AccountInfo).forEach { accountInfo ->
            var accountHolderName = accountInfo.accountHolderName1
            accountInfo.accountHolderName2?.let {
                accountHolderName += it // TODO: add a whitespace in between?
            }
            bank.customerName = accountHolderName

            findExistingAccount(bank, accountInfo)?.let { account ->
                // TODO: update AccountData. But can this ever happen that an account changes?
            }
            ?: run {
                val newAccount = AccountData(accountInfo.accountIdentifier, accountInfo.subAccountAttribute,
                    accountInfo.bankCountryCode, accountInfo.bankCode, accountInfo.iban, accountInfo.customerId,
                    accountInfo.accountType, accountInfo.currency, accountHolderName, accountInfo.productName,
                    accountInfo.accountLimit, accountInfo.allowedJobNames)

                bank.addAccount(newAccount)
            }

            // TODO: may also make use of other info
        }

        response.getFirstSegmentById<SepaAccountInfo>(InstituteSegmentId.SepaAccountInfo)?.let { sepaAccountInfo ->
            // TODO: make use of information
            sepaAccountInfo.account.iban?.let {

            }
        }

        response.getFirstSegmentById<UserParameters>(InstituteSegmentId.UserParameters)?.let { userParameters ->
            bank.updVersion = userParameters.updVersion

            if (bank.customerName.isEmpty()) {
                userParameters.username?.let {
                    bank.customerName = it
                }
            }

            // TODO: may also make use of other info
        }

        response.getFirstSegmentById<CommunicationInfo>(InstituteSegmentId.CommunicationInfo)?.let { communicationInfo ->
            if (bank.selectedLanguage != communicationInfo.defaultLanguage) {
                bank.selectedLanguage = communicationInfo.defaultLanguage
            }
        }

        val supportedJobs = response.supportedJobs
        if (supportedJobs.isNotEmpty()) { // if allowedJobsForBank is empty than bank didn't send any allowed job
            for (account in bank.accounts) {
                setAllowedJobsForAccount(bank, account, supportedJobs)
            }
        }
        else if (bank.supportedJobs.isNotEmpty()) {
            for (account in bank.accounts) {
                if (account.allowedJobs.isEmpty()) {
                    setAllowedJobsForAccount(bank, account, bank.supportedJobs)
                }
            }
        }

        if (response.supportedTanProceduresForUser.isNotEmpty()) {
            bank.tanProceduresAvailableForUser = response.supportedTanProceduresForUser.mapNotNull { findTanProcedure(it, bank) }

            if (bank.tanProceduresAvailableForUser.firstOrNull { it.securityFunction == bank.selectedTanProcedure.securityFunction } == null) { // supportedTanProcedures don't contain selectedTanProcedure anymore
                bank.resetSelectedTanProcedure()
            }
        }
    }

    protected open fun findTanProcedure(securityFunction: Sicherheitsfunktion, bank: BankData): TanProcedure? {
        return bank.tanProceduresSupportedByBank.firstOrNull { it.securityFunction == securityFunction }
    }

    protected open fun setAllowedJobsForAccount(bank: BankData, account: AccountData, supportedJobs: List<JobParameters>) {
        val allowedJobsForAccount = mutableListOf<JobParameters>()

        for (job in supportedJobs) {
            if (isJobSupported(account, job)) {
                allowedJobsForAccount.add(job)
            }
        }

        account.allowedJobs = allowedJobsForAccount

        account.setSupportsFeature(AccountFeature.RetrieveAccountTransactions, messageBuilder.supportsGetTransactions(account))
        account.setSupportsFeature(AccountFeature.RetrieveBalance, messageBuilder.supportsGetBalance(account))
        account.setSupportsFeature(AccountFeature.TransferMoney, messageBuilder.supportsBankTransfer(bank, account))
        account.setSupportsFeature(AccountFeature.InstantPayment, messageBuilder.supportsSepaInstantPaymentBankTransfer(bank, account))
    }

    protected open fun mapToTanProcedures(tanInfo: TanInfo): List<TanProcedure> {
        return tanInfo.tanProcedureParameters.procedureParameters.mapNotNull {
            mapToTanProcedure(it)
        }
    }

    protected open fun mapToTanProcedure(parameters: TanProcedureParameters): TanProcedure? {
        val procedureName = parameters.procedureName

        // we filter out iTAN and Einschritt-Verfahren as they are not permitted anymore according to PSD2
        if (procedureName.toLowerCase() == "itan") {
            return null
        }

        return TanProcedure(procedureName, parameters.securityFunction,
            mapToTanProcedureType(parameters) ?: TanProcedureType.EnterTan, mapHhdVersion(parameters),
            parameters.maxTanInputLength, parameters.allowedTanFormat,
            parameters.nameOfTanMediaRequired == BezeichnungDesTanMediumsErforderlich.BezeichnungDesTanMediumsMussAngegebenWerden)
    }

    protected open fun mapToTanProcedureType(parameters: TanProcedureParameters): TanProcedureType? {
        val name = parameters.procedureName.toLowerCase()

        return when {
            // names are like 'chipTAN (comfort) manuell', 'Smart(-)TAN plus (manuell)' and
            // technical identification is 'HHD'. Exception:  there's one that states itself as 'chipTAN (Manuell)'
            // but its ZkaTanProcedure is set to 'HHDOPT1' -> handle ChipTanManuell before ChipTanFlickercode
            parameters.zkaTanProcedure == ZkaTanProcedure.HHD || name.contains("manuell") ->
                TanProcedureType.ChipTanManuell

            // names are like 'chipTAN optisch/comfort', 'SmartTAN (plus) optic/USB', 'chipTAN (Flicker)' and
            // technical identification is 'HHDOPT1'
            parameters.zkaTanProcedure == ZkaTanProcedure.HHDOPT1 ||
                    tanProcedureNameContains(name, "optisch", "optic", "comfort", "flicker") ->
                TanProcedureType.ChipTanFlickercode

            // 'Smart-TAN plus optisch / USB' seems to be a Flickertan procedure -> test for 'optisch' first
            name.contains("usb") -> TanProcedureType.ChipTanUsb

            // QRTAN+ from 1822 direct has nothing to do with chipTAN QR.
            name.contains("qr") -> {
                if (tanProcedureNameContains(name, "chipTAN", "Smart")) TanProcedureType.ChipTanQrCode
                else TanProcedureType.QrCode
            }

            // photoTAN from Commerzbank (comdirect), Deutsche Bank, norisbank has nothing to do with chipTAN photo
            name.contains("photo") -> {
                // e.g. 'Smart-TAN photo' / description 'Challenge'
                if (tanProcedureNameContains(name, "chipTAN", "Smart")) TanProcedureType.ChipTanPhotoTanMatrixCode
                // e.g. 'photoTAN-Verfahren', description 'Freigabe durch photoTAN'
                else TanProcedureType.photoTan
            }

            tanProcedureNameContains(name, "SMS", "mobile", "mTAN") -> TanProcedureType.SmsTan

            // 'flateXSecure' identifies itself as 'PPTAN' instead of 'AppTAN'
            // 'activeTAN-Verfahren' can actually be used either with an app or a reader; it's like chipTAN QR but without a chip card
            tanProcedureNameContains(name, "push", "app", "BestSign", "SecureGo", "TAN2go", "activeTAN", "easyTAN", "SecurePlus", "TAN+")
                    || technicalTanProcedureIdentificationContains(parameters, "SECURESIGN", "PPTAN") ->
                TanProcedureType.AppTan

            // we filter out iTAN and Einschritt-Verfahren as they are not permitted anymore according to PSD2
            else -> null
        }
    }

    protected open fun mapHhdVersion(parameters: TanProcedureParameters): HHDVersion? {
        return when {
            technicalTanProcedureIdentificationContains(parameters, "HHD1.4") -> HHDVersion.HHD_1_4
            technicalTanProcedureIdentificationContains(parameters, "HHD1.3") -> HHDVersion.HHD_1_3
            parameters.versionZkaTanProcedure?.contains("1.4") == true -> HHDVersion.HHD_1_4
            parameters.versionZkaTanProcedure?.contains("1.3") == true -> HHDVersion.HHD_1_4
            else -> null
        }
    }

    protected open fun tanProcedureNameContains(name: String, vararg namesToTest: String): Boolean {
        namesToTest.forEach { nameToTest ->
            if (name.contains(nameToTest.toLowerCase())) {
                return true
            }
        }

        return false
    }

    protected open fun technicalTanProcedureIdentificationContains(parameters: TanProcedureParameters, vararg valuesToTest: String): Boolean {
        valuesToTest.forEach { valueToTest ->
            if (parameters.technicalTanProcedureIdentification.contains(valueToTest, true)) {
                return true
            }
        }

        return false
    }


    protected open fun isJobSupported(account: AccountData, supportedJob: JobParameters): Boolean {
        for (allowedJobName in account.allowedJobNames) {
            if (allowedJobName == supportedJob.jobName) {
                return true
            }
        }

        return false
    }

    protected open fun findExistingAccount(bank: BankData, accountInfo: AccountInfo): AccountData? {
        bank.accounts.forEach { account ->
            if (account.accountIdentifier == accountInfo.accountIdentifier
                && account.productName == accountInfo.productName
                && account.accountType == accountInfo.accountType) {

                return account
            }
        }

        return null
    }

}
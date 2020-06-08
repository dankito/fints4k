package net.dankito.banking.fints

import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeSpan
import com.soywiz.klock.DateTimeTz
import net.dankito.banking.fints.callback.FinTsClientCallback
import net.dankito.banking.fints.messages.MessageBuilder
import net.dankito.banking.fints.messages.MessageBuilderResult
import net.dankito.banking.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.banking.fints.messages.datenelemente.implementierte.KundensystemStatusWerte
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMedienArtVersion
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMediumKlasse
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.ZkaTanProcedure
import net.dankito.banking.fints.model.*
import net.dankito.banking.fints.response.GetUserTanProceduresResponse
import net.dankito.banking.fints.response.InstituteSegmentId
import net.dankito.banking.fints.response.Response
import net.dankito.banking.fints.response.ResponseParser
import net.dankito.banking.fints.response.client.AddAccountResponse
import net.dankito.banking.fints.response.client.FinTsClientResponse
import net.dankito.banking.fints.response.client.GetTanMediaListResponse
import net.dankito.banking.fints.response.client.GetTransactionsResponse
import net.dankito.banking.fints.response.segments.*
import net.dankito.banking.fints.tan.FlickerCodeDecoder
import net.dankito.banking.fints.tan.TanImageDecoder
import net.dankito.banking.fints.transactions.IAccountTransactionsParser
import net.dankito.banking.fints.transactions.Mt940AccountTransactionsParser
import net.dankito.banking.fints.util.IBase64Service
import net.dankito.banking.fints.util.IThreadPool
import net.dankito.banking.fints.util.PureKotlinBase64Service
import net.dankito.banking.fints.util.log.LoggerFactory
import net.dankito.banking.fints.webclient.IWebClient
import net.dankito.banking.fints.webclient.KtorWebClient
import net.dankito.banking.fints.webclient.WebClientResponse


open class FinTsClient(
    protected val callback: FinTsClientCallback,
    protected val webClient: IWebClient = KtorWebClient(),
    protected val base64Service: IBase64Service = PureKotlinBase64Service(),
    protected val threadPool: IThreadPool,
    protected val messageBuilder: MessageBuilder = MessageBuilder(),
    protected val responseParser: ResponseParser = ResponseParser(),
    protected val mt940Parser: IAccountTransactionsParser = Mt940AccountTransactionsParser(),
    protected val product: ProductData = ProductData("15E53C26816138699C7B6A3E8", "1.0.0") // TODO: get version dynamically
) {

    companion object {
        val FindAccountTransactionsStartRegex = Regex("^HIKAZ:\\d:\\d:\\d\\+@\\d+@", RegexOption.MULTILINE)
        val FindAccountTransactionsEndRegex = Regex("^-'", RegexOption.MULTILINE)


        private val log = LoggerFactory.getLogger(FinTsClient::class)
    }


    open var areWeThatGentleToCloseDialogs: Boolean = true

    protected val messageLogField = ArrayList<MessageLogEntry>() // TODO: make thread safe like with CopyOnWriteArrayList

    // in either case remove sensitive data after response is parsed as otherwise some information like account holder name and accounts may is not set yet on CustomerData
    open val messageLogWithoutSensitiveData: List<MessageLogEntry>
            get() = messageLogField.map { MessageLogEntry(removeSensitiveDataFromMessage(it.message, it.customer), it.time, it.customer) }


    /**
     * Retrieves information about bank (e.g. supported HBCI versions, FinTS server address,
     * supported jobs, ...).
     *
     * On success [bank] parameter is updated afterwards.
     */
    open fun getAnonymousBankInfoAsync(bank: BankData, callback: (FinTsClientResponse) -> Unit) {

        threadPool.runAsync {
            callback(getAnonymousBankInfo(bank))
        }
    }

    /**
     * Retrieves information about bank (e.g. supported HBCI versions, FinTS server address,
     * supported jobs, ...).
     *
     * On success [bank] parameter is updated afterwards.
     */
    open fun getAnonymousBankInfo(bank: BankData): FinTsClientResponse {
        val dialogContext = DialogContext(bank, CustomerData.Anonymous, product)

        val message = messageBuilder.createAnonymousDialogInitMessage(dialogContext)

        val response = getAndHandleResponseForMessage(message, dialogContext)

        if (response.successful) {
            updateBankData(bank, response)

            closeAnonymousDialog(dialogContext, response)
        }

        return FinTsClientResponse(response)
    }

    protected open fun closeAnonymousDialog(dialogContext: DialogContext, response: Response) {

        // bank already closed dialog -> there's no need to send dialog end message
        if (areWeThatGentleToCloseDialogs == false || dialogContext.didBankCloseDialog) {
            return
        }

        val dialogEndRequestBody = messageBuilder.createAnonymousDialogEndMessage(dialogContext)

        fireAndForgetMessage(dialogEndRequestBody, dialogContext)
    }


    open fun getBankAndCustomerInfoForNewUser(bank: BankData, customer: CustomerData): AddAccountResponse {
        // just to ensure settings are in its initial state and that bank sends use bank parameter (BPD),
        // user parameter (UPD) and allowed tan procedures for user (therefore the resetSelectedTanProcedure())
        bank.resetBpdVersion()
        customer.resetUpdVersion()
        /**
         * Sind dem Kundenprodukt die konkreten, für den Benutzer zugelassenen Sicherheitsverfahren nicht bekannt, so können
         * diese über eine Dialoginitialisierung mit Sicherheitsfunktion=999 angefordert werden. Die konkreten Verfahren
         * werden dann über den Rückmeldungscode=3920 zurückgemeldet. Im Rahmen dieses Prozesses darf keine UPD
         * zurückgeliefert werden und die Durchführung anderer Geschäftsvorfälle ist in einem solchen Dialog nicht erlaubt.
         */
        customer.resetSelectedTanProcedure()

        val dialogContext = DialogContext(bank, customer, product)

        val initDialogResponse = initDialogAfterSuccessfulChecks(dialogContext, false)

        closeDialog(dialogContext) // TODO: only close dialog if a) bank didn't close it already and b) if a global flag is set to close dialog as actually it's not necessary

        return AddAccountResponse(initDialogResponse, bank, customer)
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
    protected open fun synchronizeCustomerSystemId(bank: BankData, customer: CustomerData): FinTsClientResponse {

        val dialogContext = DialogContext(bank, customer, product)
        val message = messageBuilder.createSynchronizeCustomerSystemIdMessage(dialogContext)

        val response = getAndHandleResponseForMessage(message, dialogContext)

        if (response.successful) {
            updateBankData(bank, response)
            updateCustomerData(customer, bank, response)

            closeDialog(dialogContext)
        }

        return FinTsClientResponse(response)
    }


    open fun addAccountAsync(bank: BankData, customer: CustomerData,
                             callback: (AddAccountResponse) -> Unit) {

        threadPool.runAsync {
            callback(addAccount(bank, customer))
        }
    }

    open fun addAccount(bank: BankData, customer: CustomerData): AddAccountResponse {

        val originalAreWeThatGentleToCloseDialogs = areWeThatGentleToCloseDialogs
        areWeThatGentleToCloseDialogs = false

        /*      First dialog: Get user's basic data like her TAN procedures     */

        val newUserInfoResponse = getBankAndCustomerInfoForNewUser(bank, customer)

        if (newUserInfoResponse.isSuccessful == false) { // bank parameter (FinTS server address, ...) already seem to be wrong
            return newUserInfoResponse
        }


        // do not ask user for tan at this stage
        var didOverwriteUserUnselectedTanProcedure = false
        if (customer.isTanProcedureSelected == false && customer.supportedTanProcedures.isNotEmpty()) {

            if (customer.supportedTanProcedures.size == 1) { // user has only one TAN procedure -> set it and we're done
                customer.selectedTanProcedure = customer.supportedTanProcedures.first()
            }
            else {
                didOverwriteUserUnselectedTanProcedure = true
                customer.selectedTanProcedure = customer.supportedTanProcedures.first()
            }
        }


        /*      Second dialog: Get customer system ID - done now in getBankAndCustomerInfoForNewUser(), we try to make it without having to open an extra dialog       */


        /*      Third dialog: Get customer TAN media list - last step that can and must be done without strong customer authorization       */

        getTanMediaList(bank, customer, TanMedienArtVersion.Alle, TanMediumKlasse.AlleMedien)


        /*      Fourth dialog: Try to retrieve account transactions of last 90 days without TAN     */

        // also check if retrieving account transactions of last 90 days without tan is supported (and thereby may retrieve first account transactions)
        val transactionsOfLast90DaysResponses = mutableListOf<GetTransactionsResponse>()
        val balances = mutableMapOf<AccountData, Money>()
        customer.accounts.forEach { account ->
            if (account.supportsFeature(AccountFeature.RetrieveAccountTransactions)) {
                val response = tryGetTransactionsOfLast90DaysWithoutTan(bank, customer, account, false)
                transactionsOfLast90DaysResponses.add(response)
                response.balance?.let { balances.put(account, it) }
            }
        }

        if (didOverwriteUserUnselectedTanProcedure) {
            customer.resetSelectedTanProcedure()
        }

        areWeThatGentleToCloseDialogs = originalAreWeThatGentleToCloseDialogs

        val supportsRetrievingTransactionsOfLast90DaysWithoutTan = transactionsOfLast90DaysResponses.firstOrNull { it.isSuccessful } != null
        val unbookedTransactions = transactionsOfLast90DaysResponses.flatMap { it.unbookedTransactions }
        val bookedTransactions = transactionsOfLast90DaysResponses.flatMap { it.bookedTransactions }

        return AddAccountResponse(newUserInfoResponse.toResponse(), bank, customer,
            supportsRetrievingTransactionsOfLast90DaysWithoutTan, bookedTransactions, unbookedTransactions, balances)
    }


    /**
     * Some banks support that according to PSD2 account transactions may be retrieved without
     * a TAN (= no strong customer authorization needed).
     *
     * Check if bank supports this.
     */
    open fun tryGetTransactionsOfLast90DaysWithoutTan(bank: BankData, customer: CustomerData, account: AccountData): GetTransactionsResponse {
        return tryGetTransactionsOfLast90DaysWithoutTan(bank, customer, account, false)
    }

    protected open fun tryGetTransactionsOfLast90DaysWithoutTan(bank: BankData, customer: CustomerData, account: AccountData,
                                                      hasRetrievedTransactionsWithTanJustBefore: Boolean): GetTransactionsResponse {

        val now = DateTimeTz.nowLocal()
        val ninetyDaysAgo = now.minus(DateTimeSpan(days = 90))

        val response = getTransactions(GetTransactionsParameter(account.supportsFeature(AccountFeature.RetrieveBalance), ninetyDaysAgo.local.date, abortIfTanIsRequired = true), bank, customer, account)


        account.triedToRetrieveTransactionsOfLast90DaysWithoutTan = true

        if (response.isSuccessful) {
            if (response.isStrongAuthenticationRequired == false || hasRetrievedTransactionsWithTanJustBefore) {
                // TODO: make use of supportsRetrievingTransactionsOfLast90DaysWithoutTan in UI e.g. in updateAccountsTransactionsIfNoTanIsRequiredAsync()
                account.supportsRetrievingTransactionsOfLast90DaysWithoutTan = !!! response.isStrongAuthenticationRequired
            }
        }

        return response
    }

    open fun getTransactionsAsync(parameter: GetTransactionsParameter, bank: BankData,
                                  customer: CustomerData, account: AccountData, callback: (GetTransactionsResponse) -> Unit) {

        threadPool.runAsync {
            callback(getTransactions(parameter, bank, customer, account))
        }
    }

    open fun getTransactions(parameter: GetTransactionsParameter, bank: BankData,
                             customer: CustomerData, account: AccountData): GetTransactionsResponse {

        val dialogContext = DialogContext(bank, customer, product)

        val initDialogResponse = initDialog(dialogContext)

        if (initDialogResponse.successful == false) {
            return GetTransactionsResponse(initDialogResponse)
        }


        var balance: Money? = null

        if (parameter.alsoRetrieveBalance && account.supportsFeature(AccountFeature.RetrieveBalance)) {
            val balanceResponse = getBalanceAfterDialogInit(account, dialogContext)

            if (balanceResponse.successful == false && balanceResponse.couldCreateMessage == true) { // don't break here if required HKSAL message is not implemented
                closeDialog(dialogContext)
                return GetTransactionsResponse(balanceResponse)
            }

            balanceResponse.getFirstSegmentById<BalanceSegment>(InstituteSegmentId.Balance)?.let {
                balance = Money(it.balance, it.currency)
            }
        }


        val message = messageBuilder.createGetTransactionsMessage(parameter, account, dialogContext)

        val bookedTransactions = mutableListOf<AccountTransaction>()
        var remainingMt940String = ""

        dialogContext.abortIfTanIsRequired = parameter.abortIfTanIsRequired

        dialogContext.chunkedResponseHandler = { response ->
            response.getFirstSegmentById<ReceivedAccountTransactions>(InstituteSegmentId.AccountTransactionsMt940)?.let { transactionsSegment ->
                val (chunkTransaction, remainder) = mt940Parser.parseTransactionsChunk(remainingMt940String + transactionsSegment.bookedTransactionsString, account)

                bookedTransactions.addAll(chunkTransaction)
                remainingMt940String = remainder

                parameter.retrievedChunkListener?.invoke(bookedTransactions)
            }
        }

        val response = getAndHandleResponseForMessage(message, dialogContext)

        closeDialog(dialogContext)


        // just retrieved all transactions -> check if retrieving that ones of last 90 days is possible without entering TAN
        if (account.supportsRetrievingTransactionsOfLast90DaysWithoutTan == null &&
            response.successful && bookedTransactions.isNotEmpty() && parameter.fromDate == null) {
            tryGetTransactionsOfLast90DaysWithoutTan(bank, customer, account, true)
        }

        return GetTransactionsResponse(response,
            bookedTransactions,
            listOf(), // TODO: implement parsing MT942
            balance)
    }

    protected open fun getBalanceAfterDialogInit(account: AccountData, dialogContext: DialogContext): Response {

        val message = messageBuilder.createGetBalanceMessage(account, dialogContext)

        return getAndHandleResponseForMessage(message, dialogContext)
    }


    open fun getTanMediaListAsync(bank: BankData, customer: CustomerData,
                                  tanMediaKind: TanMedienArtVersion = TanMedienArtVersion.Alle,
                                  tanMediumClass: TanMediumKlasse = TanMediumKlasse.AlleMedien,
                                  callback: (GetTanMediaListResponse) -> Unit) {

        threadPool.runAsync {
            callback(getTanMediaList(bank, customer))
        }
    }

    open fun getTanMediaList(bank: BankData, customer: CustomerData, tanMediaKind: TanMedienArtVersion = TanMedienArtVersion.Alle,
                             tanMediumClass: TanMediumKlasse = TanMediumKlasse.AlleMedien): GetTanMediaListResponse {

        val response = sendMessageAndHandleResponse(bank, customer) { dialogContext ->
            messageBuilder.createGetTanMediaListMessage(dialogContext, tanMediaKind, tanMediumClass)
        }

        // TAN media list (= TAN generator list) is only returned for users with chipTAN TAN procedures
        val tanMediaList = if (response.successful == false ) null
                        else response.getFirstSegmentById<TanMediaList>(InstituteSegmentId.TanMediaList)

        tanMediaList?.let {
            customer.tanMedia = it.tanMedia
        }

        return GetTanMediaListResponse(response, tanMediaList)
    }


    open fun changeTanMedium(newActiveTanMedium: TanGeneratorTanMedium, bank: BankData, customer: CustomerData): FinTsClientResponse {

        var enteredAtc: EnterTanGeneratorAtcResult? = null

        if (bank.changeTanMediumParameters?.enteringAtcAndTanRequired == true) {
            enteredAtc = callback.enterTanGeneratorAtc(customer, newActiveTanMedium)

            if (enteredAtc.hasAtcBeenEntered == false) {
                val message = "Bank requires to enter ATC and TAN in order to change TAN medium." // TODO: translate
                return FinTsClientResponse(Response(false, exception = Exception(message)))
            }
        }


        val response = sendMessageAndHandleResponse(bank, customer, false) { dialogContext ->
            messageBuilder.createChangeTanMediumMessage(newActiveTanMedium, dialogContext,
                enteredAtc?.tan, enteredAtc?.atc)
        }


        return FinTsClientResponse(response)
    }


    open fun doBankTransferAsync(bankTransferData: BankTransferData, bank: BankData,
                                 customer: CustomerData, account: AccountData, callback: (FinTsClientResponse) -> Unit) {

        threadPool.runAsync {
            callback(doBankTransfer(bankTransferData, bank, customer, account))
        }
    }

    open fun doBankTransfer(bankTransferData: BankTransferData, bank: BankData,
                            customer: CustomerData, account: AccountData): FinTsClientResponse {

        val response = sendMessageAndHandleResponse(bank, customer) { dialogContext ->
            messageBuilder.createBankTransferMessage(bankTransferData, account, dialogContext)
        }

        return FinTsClientResponse(response)
    }


    protected open fun sendMessageAndHandleResponse(bank: BankData, customer: CustomerData, messageMayRequiresTan: Boolean = true,
                                                    createMessage: (DialogContext) -> MessageBuilderResult): Response {

        val dialogContext = DialogContext(bank, customer, product)

        val initDialogResponse = initDialog(dialogContext)

        if (initDialogResponse.successful == false) {
            return initDialogResponse
        }


        val message = createMessage(dialogContext)

        val response = getAndHandleResponseForMessage(message, dialogContext)

        closeDialog(dialogContext)


        return response
    }

    protected open fun initDialog(dialogContext: DialogContext): Response {

        // we first need to retrieve supported tan procedures and jobs before we can do anything
        val retrieveBasicBankDataResponse = ensureBasicBankDataRetrieved(dialogContext.bank, dialogContext.customer)
        if (retrieveBasicBankDataResponse.successful == false) {
            return retrieveBasicBankDataResponse
        }


        // as in the next step we have to supply user's tan procedure, ensure user selected his or her
        val tanProcedureSelectedResponse = ensureTanProcedureIsSelected(dialogContext.bank, dialogContext.customer)
        if (tanProcedureSelectedResponse.successful == false) {
            return tanProcedureSelectedResponse
        }

        return initDialogAfterSuccessfulChecks(dialogContext, true)
    }

    protected open fun initDialogAfterSuccessfulChecks(dialogContext: DialogContext,
                                                       useStrongAuthentication: Boolean = true): Response {

        val message = messageBuilder.createInitDialogMessage(dialogContext, useStrongAuthentication)

        val response = GetUserTanProceduresResponse(getAndHandleResponseForMessage(message, dialogContext))
        dialogContext.response = response

        if (response.successful) {
            updateBankData(dialogContext.bank, response)
            updateCustomerData(dialogContext.customer, dialogContext.bank, response)
        }

        return response
    }

    protected open fun closeDialog(dialogContext: DialogContext) {

        // bank already closed dialog -> there's no need to send dialog end message
        if (areWeThatGentleToCloseDialogs == false || dialogContext.didBankCloseDialog) {
            return
        }

        val dialogEndRequestBody = messageBuilder.createDialogEndMessage(dialogContext)

        fireAndForgetMessage(dialogEndRequestBody, dialogContext)
    }


    protected open fun ensureBasicBankDataRetrieved(bank: BankData, customer: CustomerData): Response {
        if (bank.supportedTanProcedures.isEmpty() || bank.supportedJobs.isEmpty()) {
            val getBankInfoResponse = getBankAndCustomerInfoForNewUser(bank, customer)

            if (getBankInfoResponse.isSuccessful == false || bank.supportedTanProcedures.isEmpty()
                || bank.supportedJobs.isEmpty()) {

                return Response(false, exception =
                Exception("Could not retrieve basic bank data like supported tan procedures or supported jobs")) // TODO: translate // TODO: add as messageToShowToUser
            }
        }

        return Response(true)
    }

    protected open fun ensureTanProcedureIsSelected(bank: BankData, customer: CustomerData): Response {
        if (customer.isTanProcedureSelected == false) {
            if (customer.supportedTanProcedures.isEmpty()) {
                getBankAndCustomerInfoForNewUser(bank, customer)
            }

            if (customer.supportedTanProcedures.isEmpty()) { // could not retrieve supported tan procedures for user
                return Response(false, noTanProcedureSelected = true)
            }

            if (customer.supportedTanProcedures.size == 1) { // user has only one TAN procedure -> set it and we're done
                customer.selectedTanProcedure = customer.supportedTanProcedures.first()
            }
            else {
                // we know user's supported tan procedures, now ask user which one to select
                callback.askUserForTanProcedure(customer.supportedTanProcedures, selectSuggestedTanProcedure(customer))?.let {
                    customer.selectedTanProcedure = it
                }
            }
        }

        return Response(customer.isTanProcedureSelected, noTanProcedureSelected = !!!customer.isTanProcedureSelected)
    }

    protected open fun selectSuggestedTanProcedure(customer: CustomerData): TanProcedure? {
        return customer.supportedTanProcedures.firstOrNull { it.type != TanProcedureType.ChipTanUsb && it.type != TanProcedureType.SmsTan && it.type != TanProcedureType.ChipTanManuell }
            ?: customer.supportedTanProcedures.firstOrNull { it.type != TanProcedureType.ChipTanUsb && it.type != TanProcedureType.SmsTan }
            ?: customer.supportedTanProcedures.firstOrNull { it.type != TanProcedureType.ChipTanUsb }
            ?: customer.supportedTanProcedures.firstOrNull()
    }


    protected open fun getAndHandleResponseForMessage(message: MessageBuilderResult, dialogContext: DialogContext): Response {
        val response = if (message.createdMessage == null) Response(false, messageCreationError = message)
                        else getAndHandleResponseForMessage(message.createdMessage, dialogContext)

        val handledResponse = handleMayRequiresTan(response, dialogContext)

        // if there's a Aufsetzpunkt (continuationId) set, then response is not complete yet, there's more information to fetch by sending this Aufsetzpunkt
        handledResponse.aufsetzpunkt?.let { continuationId ->
            if (handledResponse.followUpResponse == null) { // for re-sent messages followUpResponse is already set and dialog already closed -> would be overwritten with an error response that dialog is closed
                if (message.isSendEnteredTanMessage() == false) { // for sending TAN no follow up message can be created -> filter out, otherwise chunkedResponseHandler would get called twice for same response
                    dialogContext.chunkedResponseHandler?.invoke(handledResponse)
                }

                handledResponse.followUpResponse = getFollowUpMessageForContinuationId(handledResponse, continuationId, message, dialogContext)

                handledResponse.hasFollowUpMessageButCouldNotReceiveIt = handledResponse.followUpResponse == null
            }
        }
        ?: run { dialogContext.chunkedResponseHandler?.invoke(handledResponse) }

        return handledResponse
    }

    protected open fun getAndHandleResponseForMessage(requestBody: String, dialogContext: DialogContext): Response {
        addMessageLog(requestBody, MessageLogEntryType.Sent, dialogContext)

        val webResponse = getResponseForMessage(requestBody, dialogContext.bank.finTs3ServerAddress)

        val response = handleResponse(webResponse, dialogContext)

        dialogContext.response = response

        response.messageHeader?.let { header -> dialogContext.dialogId = header.dialogId }
        dialogContext.didBankCloseDialog = response.didBankCloseDialog

        return response
    }

    protected open fun getResponseForMessage(requestBody: String, finTs3ServerAddress: String): WebClientResponse {
        val encodedRequestBody = base64Service.encode(requestBody)

        return webClient.post(finTs3ServerAddress, encodedRequestBody, "application/octet-stream")
    }

    protected open fun fireAndForgetMessage(message: MessageBuilderResult, dialogContext: DialogContext) {
        message.createdMessage?.let { requestBody ->
            addMessageLog(requestBody, MessageLogEntryType.Sent, dialogContext)

            getResponseForMessage(requestBody, dialogContext.bank.finTs3ServerAddress)

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

                return Response(false, exception = e)
            }
        }
        else {
            val bank = dialogContext.bank
            log.error { "Request to $bank (${bank.finTs3ServerAddress}) failed" } // TODO: add webResponse.error
        }

        return Response(false, exception = webResponse.error)
    }

    protected open fun decodeBase64Response(responseBody: String): String {
        return base64Service.decode(responseBody.replace("\r", "").replace("\n", ""))
    }


    protected open fun getFollowUpMessageForContinuationId(response: Response, continuationId: String, message: MessageBuilderResult,
                                                           dialogContext: DialogContext): Response? {

        messageBuilder.rebuildMessageWithContinuationId(message, continuationId, dialogContext)?.let { followUpMessage ->
            return getAndHandleResponseForMessage(followUpMessage, dialogContext)
        }

        return null
    }


    protected open fun addMessageLog(message: String, type: MessageLogEntryType, dialogContext: DialogContext) {
        val timeStamp = DateTime.now()
        val messagePrefix = "${if (type == MessageLogEntryType.Sent) "Sending" else "Received"} message:\r\n" // currently no need to translate
        val prettyPrintMessage = prettyPrintHbciMessage(message)
        val prettyPrintMessageWithPrefix = "$messagePrefix$prettyPrintMessage"

        log.debug { prettyPrintMessageWithPrefix }

        messageLogField.add(MessageLogEntry(prettyPrintMessageWithPrefix, timeStamp, dialogContext.customer))
    }

    protected fun prettyPrintHbciMessage(message: String): String {
        return message.replace("'", "'\r\n")
    }

    protected open fun removeSensitiveDataFromMessage(message: String, customer: CustomerData): String {
        var prettyPrintMessageWithoutSensitiveData = message
            .replace(customer.customerId, "<customer_id>")
            .replace("+" + customer.pin, "+<pin>")

        if (customer.name.isNotBlank()) {
            prettyPrintMessageWithoutSensitiveData = prettyPrintMessageWithoutSensitiveData
                .replace(customer.name, "<customer_name>", true)
        }

        customer.accounts.forEach { account ->
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


    protected open fun handleMayRequiresTan(response: Response, dialogContext: DialogContext): Response { // TODO: use response from DialogContext

        if (response.isStrongAuthenticationRequired) {
            if (dialogContext.abortIfTanIsRequired) {
                response.tanRequiredButWeWereToldToAbortIfSo = true

                return response
            }

            response.tanResponse?.let { tanResponse ->
                val customer = dialogContext.customer
                val enteredTanResult = callback.enterTan(customer, createTanChallenge(tanResponse, customer))

                if (enteredTanResult.changeTanProcedureTo != null) {
                    return handleUserAsksToChangeTanProcedureAndResendLastMessage(enteredTanResult.changeTanProcedureTo,
                        dialogContext)
                }
                else if (enteredTanResult.changeTanMediumTo is TanGeneratorTanMedium) {
                    return handleUserAsksToChangeTanMediumAndResendLastMessage(enteredTanResult.changeTanMediumTo,
                        dialogContext, enteredTanResult.changeTanMediumResultCallback)
                }
                else if (enteredTanResult.enteredTan == null) {
                    // i tried to send a HKTAN with cancelJob = true but then i saw there are no tan procedures that support cancellation (at least not at my bank)
                    // but it's not required anyway, tan times out after some time. Simply don't respond anything and close dialog
                    response.tanRequiredButUserDidNotEnterOne = true
                }
                else {
                    return sendTanToBank(enteredTanResult.enteredTan, tanResponse, dialogContext)
                }
            }
        }

        // TODO: check if response contains '3931 TAN-Generator gesperrt, Synchronisierung erforderlich' or
        //  '3933 TAN-Generator gesperrt, Synchronisierung erforderlich Kartennummer ##########' message,
        //  call callback.enterAtc() and implement and call HKTSY job  (p. 77)

        // TODO: also check '9931 Sperrung des Kontos nach %1 Fehlversuchen' -> if %1 == 3 synchronize TAN generator
        //  as it's quite unrealistic that user entered TAN wrong three times, in most cases TAN generator is not synchronized

        return response
    }

    protected open fun createTanChallenge(tanResponse: TanResponse, customer: CustomerData): TanChallenge {
        // TODO: is this true for all tan procedures?
        val messageToShowToUser = tanResponse.challenge ?: ""
        val challenge = tanResponse.challengeHHD_UC ?: ""
        val tanProcedure = customer.selectedTanProcedure

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

    protected open fun sendTanToBank(enteredTan: String, tanResponse: TanResponse, dialogContext: DialogContext): Response {

        val message = messageBuilder.createSendEnteredTanMessage(enteredTan, tanResponse, dialogContext)

        return getAndHandleResponseForMessage(message, dialogContext)
    }

    protected open fun handleUserAsksToChangeTanProcedureAndResendLastMessage(changeTanProcedureTo: TanProcedure, dialogContext: DialogContext): Response {

        dialogContext.customer.selectedTanProcedure = changeTanProcedureTo


        val lastCreatedMessage = dialogContext.currentMessage

        lastCreatedMessage?.let { closeDialog(dialogContext) }

        return resendMessageInNewDialog(lastCreatedMessage, dialogContext)
    }

    protected open fun handleUserAsksToChangeTanMediumAndResendLastMessage(changeTanMediumTo: TanGeneratorTanMedium,
                                                                           dialogContext: DialogContext,
                                                                           changeTanMediumResultCallback: ((FinTsClientResponse) -> Unit)?): Response {

        val lastCreatedMessage = dialogContext.currentMessage

        lastCreatedMessage?.let { closeDialog(dialogContext) }


        val changeTanMediumResponse = changeTanMedium(changeTanMediumTo, dialogContext.bank, dialogContext.customer)

        changeTanMediumResultCallback?.invoke(changeTanMediumResponse)

        if (changeTanMediumResponse.isSuccessful == false || lastCreatedMessage == null) {
            return changeTanMediumResponse.toResponse()
        }


        return resendMessageInNewDialog(lastCreatedMessage, dialogContext)
    }


    protected open fun resendMessageInNewDialog(lastCreatedMessage: MessageBuilderResult?, previousDialogContext: DialogContext): Response {

        lastCreatedMessage?.let { // do not use previousDialogContext.currentMessage as this may is previous dialog's dialog close message
            val newDialogContext = DialogContext(previousDialogContext.bank, previousDialogContext.customer, previousDialogContext.product, chunkedResponseHandler = previousDialogContext.chunkedResponseHandler)

            val initDialogResponse = initDialog(newDialogContext)
            if (initDialogResponse.successful == false) {
                return initDialogResponse
            }


            val newMessage = messageBuilder.rebuildMessage(lastCreatedMessage, newDialogContext)

            val response = getAndHandleResponseForMessage(newMessage, newDialogContext)

            closeDialog(newDialogContext)

            return response
        }

        val errorMessage = "There's no last action (like retrieve account transactions, transfer money, ...) to re-send with new TAN procedure. Probably an internal programming error." // TODO: translate
        return Response(false, exception = Exception(errorMessage)) // should never come to this
    }


    protected open fun updateBankData(bank: BankData, response: Response) {
        response.getFirstSegmentById<BankParameters>(InstituteSegmentId.BankParameters)?.let { bankParameters ->
            bank.bpdVersion = bankParameters.bpdVersion
            bank.name = bankParameters.bankName
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
            bank.supportedTanProcedures = mapToTanProcedures(tanInfo)
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

    protected open fun updateCustomerData(customer: CustomerData, bank: BankData, response: Response) {
        response.getFirstSegmentById<BankParameters>(InstituteSegmentId.BankParameters)?.let { bankParameters ->
            // TODO: ask user if there is more than one supported language? But it seems that almost all banks only support German.
            if (customer.selectedLanguage == Dialogsprache.Default && bankParameters.supportedLanguages.isNotEmpty()) {
                customer.selectedLanguage = bankParameters.supportedLanguages.first()
            }
        }

        response.getFirstSegmentById<ReceivedSynchronization>(InstituteSegmentId.Synchronization)?.let { synchronization ->
            synchronization.customerSystemId?.let {
                customer.customerSystemId = it

                customer.customerSystemStatus = KundensystemStatusWerte.Benoetigt // TODO: didn't find out for sure yet, but i think i read somewhere, that this has to be set when customerSystemId is set
            }
        }

        response.getSegmentsById<AccountInfo>(InstituteSegmentId.AccountInfo).forEach { accountInfo ->
            var accountHolderName = accountInfo.accountHolderName1
            accountInfo.accountHolderName2?.let {
                accountHolderName += it // TODO: add a whitespace in between?
            }
            customer.name = accountHolderName

            findExistingAccount(customer, accountInfo)?.let { account ->
                // TODO: update AccountData. But can this ever happen that an account changes?
            }
            ?: run {
                val newAccount = AccountData(accountInfo.accountIdentifier, accountInfo.subAccountAttribute,
                    accountInfo.bankCountryCode, accountInfo.bankCode, accountInfo.iban, accountInfo.customerId,
                    accountInfo.accountType, accountInfo.currency, accountHolderName, accountInfo.productName,
                    accountInfo.accountLimit, accountInfo.allowedJobNames)

                customer.addAccount(newAccount)
            }

            // TODO: may also make use of other info
        }

        response.getFirstSegmentById<SepaAccountInfo>(InstituteSegmentId.SepaAccountInfo)?.let { sepaAccountInfo ->
            // TODO: make use of information
            sepaAccountInfo.account.iban?.let {

            }
        }

        response.getFirstSegmentById<UserParameters>(InstituteSegmentId.UserParameters)?.let { userParameters ->
            customer.updVersion = userParameters.updVersion

            if (customer.name.isEmpty()) {
                userParameters.username?.let {
                    customer.name = it
                }
            }

            // TODO: may also make use of other info
        }

        response.getFirstSegmentById<CommunicationInfo>(InstituteSegmentId.CommunicationInfo)?.let { communicationInfo ->
            if (customer.selectedLanguage != communicationInfo.defaultLanguage) {
                customer.selectedLanguage = communicationInfo.defaultLanguage
            }
        }

        val supportedJobs = response.supportedJobs
        if (supportedJobs.isNotEmpty()) { // if allowedJobsForBank is empty than bank didn't send any allowed job
            for (account in customer.accounts) {
                setAllowedJobsForAccount(bank, account, supportedJobs)
            }
        }
        else if (bank.supportedJobs.isNotEmpty()) {
            for (account in customer.accounts) {
                if (account.allowedJobs.isEmpty()) {
                    setAllowedJobsForAccount(bank, account, bank.supportedJobs)
                }
            }
        }

        if (response.supportedTanProceduresForUser.isNotEmpty()) {
            customer.supportedTanProcedures = response.supportedTanProceduresForUser.mapNotNull { findTanProcedure(it, bank) }
        }
    }

    protected open fun findTanProcedure(securityFunction: Sicherheitsfunktion, bank: BankData): TanProcedure? {
        return bank.supportedTanProcedures.firstOrNull { it.securityFunction == securityFunction }
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
            mapToTanProcedureType(parameters) ?: TanProcedureType.EnterTan, mapHhdVersion(parameters))
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

    protected open fun findExistingAccount(customer: CustomerData, accountInfo: AccountInfo): AccountData? {
        customer.accounts.forEach { account ->
            if (account.accountIdentifier == accountInfo.accountIdentifier
                && account.productName == accountInfo.productName
                && account.accountType == accountInfo.accountType) {

                return account
            }
        }

        return null
    }

}
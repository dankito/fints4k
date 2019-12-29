package net.dankito.fints

import net.dankito.fints.messages.MessageBuilder
import net.dankito.fints.messages.MessageBuilderResult
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemID
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatusWerte
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedienArtVersion
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanMediumKlasse
import net.dankito.fints.model.*
import net.dankito.fints.response.InstituteSegmentId
import net.dankito.fints.response.Response
import net.dankito.fints.response.ResponseParser
import net.dankito.fints.response.client.AddAccountResponse
import net.dankito.fints.response.client.FinTsClientResponse
import net.dankito.fints.response.client.GetTanMediaListResponse
import net.dankito.fints.response.client.GetTransactionsResponse
import net.dankito.fints.response.segments.*
import net.dankito.fints.util.IBase64Service
import net.dankito.utils.IThreadPool
import net.dankito.utils.ThreadPool
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.OkHttpWebClient
import net.dankito.utils.web.client.RequestParameters
import net.dankito.utils.web.client.WebClientResponse
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*


open class FinTsClient @JvmOverloads constructor(
    protected val callback: FinTsClientCallback,
    protected val base64Service: IBase64Service,
    protected val webClient: IWebClient = OkHttpWebClient(),
    protected val messageBuilder: MessageBuilder = MessageBuilder(),
    protected val responseParser: ResponseParser = ResponseParser(),
    protected val threadPool: IThreadPool = ThreadPool(),
    protected val product: ProductData = ProductData("15E53C26816138699C7B6A3E8", "0.1") // TODO: get version dynamically
) {

    companion object {
        const val NinetyDaysAgoMilliseconds = 90 * 24 * 60 * 60 * 1000L

        private val log = LoggerFactory.getLogger(FinTsClient::class.java)
    }


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
        val dialogData = DialogData()

        val requestBody = messageBuilder.createAnonymousDialogInitMessage(bank, product, dialogData)

        val response = getAndHandleResponseForMessage(requestBody, bank)

        if (response.successful) {
            updateBankData(bank, response)

            closeAnonymousDialog(dialogData, response, bank)
        }

        return FinTsClientResponse(response)
    }

    protected open fun closeAnonymousDialog(dialogData: DialogData, response: Response, bank: BankData) {
        dialogData.increaseMessageNumber()

        response.messageHeader?.let { header -> dialogData.dialogId = header.dialogId }

        val dialogEndRequestBody = messageBuilder.createAnonymousDialogEndMessage(bank, dialogData)

        getAndHandleResponseForMessage(dialogEndRequestBody, bank)
    }


    open fun getBankAndCustomerInfoForNewUser(bank: BankData, customer: CustomerData): AddAccountResponse {
        val dialogData = DialogData()

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

        val initDialogResponse = initDialogWithoutChecks(bank, customer, dialogData, false)

        closeDialog(bank, customer, dialogData)

        return AddAccountResponse(initDialogResponse, bank, customer)
    }


    open fun addAccountAsync(bank: BankData, customer: CustomerData,
                             callback: (AddAccountResponse) -> Unit) {

        threadPool.runAsync {
            callback(addAccount(bank, customer))
        }
    }

    open fun addAccount(bank: BankData, customer: CustomerData): AddAccountResponse {

        val newUserInfoResponse = getBankAndCustomerInfoForNewUser(bank, customer)

        if (newUserInfoResponse.isSuccessful == false) { // bank parameter (FinTS server address, ...) already seem to be wrong
            return newUserInfoResponse
        }


        // do not ask user for tan at this stage
        var didOverwriteUserUnselectedTanProcedure = false
        if (customer.isTanProcedureSelected == false && customer.supportedTanProcedures.isNotEmpty()) {

            didOverwriteUserUnselectedTanProcedure = true
            customer.selectedTanProcedure = customer.supportedTanProcedures.first()
        }


        val synchronizeCustomerResponse = synchronizeCustomerSystemId(bank, customer)

        // also check if retrieving account transactions of last 90 days without tan is supported (and thereby may retrieve first account transactions)
        val transactionsOfLast90DaysResponse =
            tryGetTransactionsOfLast90DaysWithoutTan(bank, customer, true)


        if (didOverwriteUserUnselectedTanProcedure) {
            customer.resetSelectedTanProcedure()
        }

        return AddAccountResponse(synchronizeCustomerResponse.toResponse(), bank, customer,
            transactionsOfLast90DaysResponse.isSuccessful,
            transactionsOfLast90DaysResponse.bookedTransactions,
            transactionsOfLast90DaysResponse.unbookedTransactions,
            transactionsOfLast90DaysResponse.balance)
    }


    /**
     * Some banks support that according to PSD2 account transactions may be retrieved without
     * a TAN (= no strong customer authorization needed).
     */
    open fun tryGetTransactionsOfLast90DaysWithoutTanAsync(bank: BankData, customer: CustomerData,
                                                           callback: (GetTransactionsResponse) -> Unit) {

        threadPool.runAsync {
            callback(tryGetTransactionsOfLast90DaysWithoutTan(bank, customer))
        }
    }

    /**
     * Some banks support that according to PSD2 account transactions may be retrieved without
     * a TAN (= no strong customer authorization needed).
     */
    open fun tryGetTransactionsOfLast90DaysWithoutTan(bank: BankData, customer: CustomerData): GetTransactionsResponse {

        return tryGetTransactionsOfLast90DaysWithoutTan(bank, customer, false)
    }

    protected open fun tryGetTransactionsOfLast90DaysWithoutTan(bank: BankData, customer: CustomerData,
                                                      skipSettingCustomerFlag: Boolean): GetTransactionsResponse {

        val ninetyDaysAgo = Date(Date().time - NinetyDaysAgoMilliseconds)

        val response = getTransactions(
            GetTransactionsParameter(true, ninetyDaysAgo), bank, customer)


        customer.triedToRetrieveTransactionsOfLast90DaysWithoutTan = true

        if (response.isSuccessful) {
            if (skipSettingCustomerFlag == false) {
                customer.supportsRetrievingTransactionsOfLast90DaysWithoutTan =
                    response.isStrongAuthenticationRequired
            }
        }

        return response
    }

    open fun getTransactionsAsync(parameter: GetTransactionsParameter, bank: BankData,
                                  customer: CustomerData, callback: (GetTransactionsResponse) -> Unit) {

        threadPool.runAsync {
            callback(getTransactions(parameter, bank, customer))
        }
    }

    open fun getTransactions(parameter: GetTransactionsParameter, bank: BankData,
                             customer: CustomerData): GetTransactionsResponse {

        if (customer.supportsRetrievingTransactionsOfLast90DaysWithoutTan == null &&
                customer.triedToRetrieveTransactionsOfLast90DaysWithoutTan == false &&
                parameter.fromDate == null) {
            tryGetTransactionsOfLast90DaysWithoutTan(bank, customer, true)
        }


        val dialogData = DialogData()

        val initDialogResponse = initDialog(bank, customer, dialogData)

        if (initDialogResponse.successful == false) {
            return GetTransactionsResponse(initDialogResponse)
        }


        var balance: BigDecimal? = null

        if (parameter.alsoRetrieveBalance) {
            val balanceResponse = getBalanceAfterDialogInit(bank, customer, dialogData)

            if (balanceResponse.successful == false && balanceResponse.couldCreateMessage == true) { // don't break here if required HKSAL message is not implemented
                closeDialog(bank, customer, dialogData)
                return GetTransactionsResponse(balanceResponse)
            }

            balanceResponse.getFirstSegmentById<BalanceSegment>(InstituteSegmentId.Balance)?.let {
                balance = it.balance
            }
        }


        dialogData.increaseMessageNumber()

        val message = messageBuilder.createGetTransactionsMessage(parameter, bank, customer, product, dialogData)

        val response = getAndHandleResponseForMessageThatMayRequiresTan(message, bank, customer, dialogData)

        closeDialog(bank, customer, dialogData)


        response.getFirstSegmentById<ReceivedAccountTransactions>(InstituteSegmentId.AccountTransactionsMt940)?.let { transactions ->
            // TODO: that should not work. Find out in which method transactions are retrieved after entering TAN
            // just retrieved all transactions -> check if retrieving that ones of last 90 days is possible without entering TAN
            if (customer.supportsRetrievingTransactionsOfLast90DaysWithoutTan == null &&
                response.successful && transactions.bookedTransactions.isNotEmpty() && parameter.fromDate == null) {
                tryGetTransactionsOfLast90DaysWithoutTan(bank, customer)
            }

            val bookedAndUnbookedTransactions = getTransactionsFromResponse(response, transactions)

            return GetTransactionsResponse(response,
                bookedAndUnbookedTransactions.first.sortedByDescending { it.bookingDate },
                bookedAndUnbookedTransactions.second,
                balance)
        }

        return GetTransactionsResponse(response)
    }

    protected open fun getTransactionsFromResponse(response: Response, transactions: ReceivedAccountTransactions): Pair<List<AccountTransaction>, List<Any>> {
        val bookedTransactions = mutableListOf<AccountTransaction>()
        val unbookedTransactions = mutableListOf<Any>()

        bookedTransactions.addAll(transactions.bookedTransactions)
        unbookedTransactions.addAll(transactions.unbookedTransactions)

        response.followUpResponse?.let { followUpResponse ->
            followUpResponse.getFirstSegmentById<ReceivedAccountTransactions>(InstituteSegmentId.AccountTransactionsMt940)?.let { followUpTransactions ->
                val followUpBookedAndUnbookedTransactions = getTransactionsFromResponse(followUpResponse, followUpTransactions)

                bookedTransactions.addAll(followUpBookedAndUnbookedTransactions.first)
                unbookedTransactions.addAll(followUpBookedAndUnbookedTransactions.second)
            }
        }

        return Pair(bookedTransactions, unbookedTransactions)
    }

    protected open fun getBalanceAfterDialogInit(bank: BankData, customer: CustomerData,
                                                 dialogData: DialogData): Response {

        dialogData.increaseMessageNumber()

        val balanceRequest = messageBuilder.createGetBalanceMessage(bank, customer, product, dialogData)

        return getAndHandleResponseForMessage(balanceRequest, bank)
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

        val dialogData = DialogData()

        val initDialogResponse = initDialog(bank, customer, dialogData)

        if (initDialogResponse.successful == false) {
            return GetTanMediaListResponse(initDialogResponse, null)
        }


        dialogData.increaseMessageNumber()

        val message = messageBuilder.createGetTanMediaListMessage(bank, customer, dialogData, tanMediaKind, tanMediumClass)

        val response = getAndHandleResponseForMessageThatMayRequiresTan(message, bank, customer, dialogData)

        closeDialog(bank, customer, dialogData)

        val tanMediaList = response.getFirstSegmentById<TanMediaList>(InstituteSegmentId.TanMediaList)

        return GetTanMediaListResponse(response, tanMediaList)
    }


    open fun doBankTransferAsync(bankTransferData: BankTransferData, bank: BankData,
                                 customer: CustomerData, callback: (FinTsClientResponse) -> Unit) {

        threadPool.runAsync {
            callback(doBankTransfer(bankTransferData, bank, customer))
        }
    }

    open fun doBankTransfer(bankTransferData: BankTransferData, bank: BankData,
                            customer: CustomerData): FinTsClientResponse {

        val dialogData = DialogData()

        val initDialogResponse = initDialog(bank, customer, dialogData)

        if (initDialogResponse.successful == false) {
            return FinTsClientResponse(initDialogResponse)
        }


        dialogData.increaseMessageNumber()

        val message = messageBuilder.createBankTransferMessage(bankTransferData, bank, customer, dialogData)

        val response = getAndHandleResponseForMessageThatMayRequiresTan(message, bank, customer, dialogData)

        closeDialog(bank, customer, dialogData)

        return FinTsClientResponse(response)
    }


    protected open fun initDialog(bank: BankData, customer: CustomerData, dialogData: DialogData): Response {

        // we first need to retrieve supported tan procedures and jobs before we can do anything
        val retrieveBasicBankDataResponse = ensureBasicBankDataRetrieved(bank, customer)
        if (retrieveBasicBankDataResponse.successful == false) {
            return retrieveBasicBankDataResponse
        }


        // as in the next step we have to supply user's tan procedure, ensure user selected his or her
        val tanProcedureSelectedResponse = ensureTanProcedureIsSelected(bank, customer)
        if (tanProcedureSelectedResponse.successful == false) {
            return tanProcedureSelectedResponse
        }

        return initDialogWithoutChecks(bank, customer, dialogData, true)
    }

    protected open fun initDialogWithoutChecks(bank: BankData, customer: CustomerData, dialogData: DialogData,
                                               useStrongAuthentication: Boolean = true): Response {

        val requestBody = messageBuilder.createInitDialogMessage(bank, customer, product, dialogData, useStrongAuthentication)

        val response = getAndHandleResponseForMessage(requestBody, bank)

        if (response.successful) {
            updateBankData(bank, response)
            updateCustomerData(customer, bank, response)

            response.messageHeader?.let { header -> dialogData.dialogId = header.dialogId }
        }

        return response
    }

    protected open fun closeDialog(bank: BankData, customer: CustomerData, dialogData: DialogData) {
        dialogData.increaseMessageNumber()

        val dialogEndRequestBody = messageBuilder.createDialogEndMessage(bank, customer, dialogData)

        getAndHandleResponseForMessage(dialogEndRequestBody, bank)
    }


    protected open fun synchronizeCustomerSystemIdIfNotDoneYet(bank: BankData,
                                                               customer: CustomerData): FinTsClientResponse {

        if (customer.customerSystemId == KundensystemID.Anonymous) { // customer system id not synchronized yet
            return synchronizeCustomerSystemId(bank, customer)
        }

        return FinTsClientResponse(true, true, false)
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

        val dialogData = DialogData()
        val requestBody = messageBuilder.createSynchronizeCustomerSystemIdMessage(bank, customer, product, dialogData)

        val response = getAndHandleResponseForMessage(requestBody, bank)

        if (response.successful) {
            updateBankData(bank, response)
            updateCustomerData(customer, bank, response)

            response.messageHeader?.let { header -> dialogData.dialogId = header.dialogId }

            closeDialog(bank, customer, dialogData)
        }

        return FinTsClientResponse(response)
    }


    protected open fun ensureBasicBankDataRetrieved(bank: BankData, customer: CustomerData): Response {
        if (bank.supportedTanProcedures.isEmpty() || bank.supportedJobs.isEmpty()) {
            bank.resetBpdVersion()

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

            // we know user's supported tan procedure, now ask user which one to select
            callback.askUserForTanProcedure(customer.supportedTanProcedures)?.let {
                customer.selectedTanProcedure = it
            }
        }

        return Response(customer.isTanProcedureSelected, noTanProcedureSelected = !!!customer.isTanProcedureSelected)
    }


    protected open fun getAndHandleResponseForMessageThatMayRequiresTan(message: MessageBuilderResult, bank: BankData,
                                                                        customer: CustomerData, dialogData: DialogData): Response {
        val response = getAndHandleResponseForMessage(message, bank)

        val handledResponse = handleMayRequiredTan(response, bank, customer, dialogData)

        // if there's a Aufsetzpunkt (continuationId) set, then response is not complete yet, there's more information to fetch by sending this Aufsetzpunkt
        handledResponse.aufsetzpunkt?.let { continuationId ->
            handledResponse.followUpResponse = getFollowUpMessageForContinuationId(handledResponse, continuationId, message, bank, customer, dialogData)

            handledResponse.hasFollowUpMessageButCouldNotReceiveIt = handledResponse.followUpResponse == null
        }

        return handledResponse
    }

    protected open fun getFollowUpMessageForContinuationId(response: Response, continuationId: String, message: MessageBuilderResult,
                                                           bank: BankData, customer: CustomerData, dialogData: DialogData): Response? {

        messageBuilder.rebuildMessageWithContinuationId(message, continuationId, bank, customer, dialogData)?.let { followUpMessage ->
            return getAndHandleResponseForMessageThatMayRequiresTan(followUpMessage, bank, customer, dialogData)
        }

        return null
    }

    protected open fun getAndHandleResponseForMessageThatMayRequiresTan(message: String, bank: BankData,
                                                                        customer: CustomerData, dialogData: DialogData): Response {
        val response = getAndHandleResponseForMessage(message, bank)

        return handleMayRequiredTan(response, bank, customer, dialogData)
    }

    protected open fun getAndHandleResponseForMessage(message: MessageBuilderResult, bank: BankData): Response {
        message.createdMessage?.let { requestBody ->
            return getAndHandleResponseForMessage(requestBody, bank)
        }

        return Response(false, messageCreationError = message)
    }

    protected open fun getAndHandleResponseForMessage(requestBody: String, bank: BankData): Response {
        val webResponse = getResponseForMessage(requestBody, bank)

        return handleResponse(webResponse, bank)
    }

    protected open fun getResponseForMessage(requestBody: String, bank: BankData): WebClientResponse {
        log.debug("Sending message:\n$requestBody")

        val encodedRequestBody = base64Service.encode(requestBody)

        return webClient.post(
            RequestParameters(bank.finTs3ServerAddress, encodedRequestBody, "application/octet-stream")
        )
    }

    protected open fun handleResponse(webResponse: WebClientResponse, bank: BankData): Response {
        val responseBody = webResponse.body

        if (webResponse.isSuccessful && responseBody != null) {

            val decodedResponse = decodeBase64Response(responseBody)

            log.debug("Received message:\n$decodedResponse")

            return responseParser.parse(decodedResponse)
        }
        else {
            log.error("Request to $bank (${bank.finTs3ServerAddress}) failed", webResponse.error)
        }

        return Response(false, exception = webResponse.error)
    }

    protected open fun decodeBase64Response(responseBody: String): String {
        return base64Service.decode(responseBody.replace("\r", "").replace("\n", ""))
    }

    protected open fun handleMayRequiredTan(response: Response, bank: BankData, customer: CustomerData, dialogData: DialogData): Response {
        if (response.isStrongAuthenticationRequired) {
            response.tanResponse?.let { tanResponse ->
                // TODO: is this true for all tan procedures?
                val enteredTan = callback.enterTan(TanChallenge(tanResponse.challenge ?: "",
                    tanResponse.challengeHHD_UC ?: "", customer.selectedTanProcedure))

                if (enteredTan == null) {
                    // i tried to send a HKTAN with cancelJob = true but then i saw there are no tan procedures that support cancellation (at least not at my bank)
                    // but it's not required anyway, tan times out after some time. Simply don't respond anything and close dialog
                    response.tanRequiredButNotProvided = true
                }
                else {
                    return sendTanToBank(enteredTan, tanResponse, bank, customer, dialogData)
                }
            }
        }

        return response
    }

    protected open fun sendTanToBank(enteredTan: String, tanResponse: TanResponse, bank: BankData,
                                     customer: CustomerData, dialogData: DialogData): Response {

        dialogData.increaseMessageNumber()

        val message = messageBuilder.createSendEnteredTanMessage(enteredTan, tanResponse, bank, customer, dialogData)

        return getAndHandleResponseForMessageThatMayRequiresTan(message, bank, customer, dialogData)
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

        response.getFirstSegmentById<AccountInfo>(InstituteSegmentId.AccountInfo)?.let { accountInfo ->
            customer.iban = accountInfo.iban // TODO: remove and use that one from AccountData

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

                val accounts = customer.accounts.toMutableList()
                accounts.add(newAccount)
                customer.accounts = accounts
            }

            // TODO: may also make use of other info
        }

        response.getFirstSegmentById<SepaAccountInfo>(InstituteSegmentId.SepaAccountInfo)?.let { sepaAccountInfo ->
            // TODO: may also make use of other info
            sepaAccountInfo.account.iban?.let {
                customer.iban = it
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
                customer.selectedLanguage == communicationInfo.defaultLanguage
            }
        }

        val supportedJobs = response.supportedJobs
        if (supportedJobs.isNotEmpty()) { // if allowedJobsForBank is empty than bank didn't send any allowed job
            for (account in customer.accounts) {
                setAllowedJobsForAccount(account, supportedJobs)
            }
        }
        else if (bank.supportedJobs.isNotEmpty()) {
            for (account in customer.accounts) {
                if (account.allowedJobs.isEmpty()) {
                    setAllowedJobsForAccount(account, bank.supportedJobs)
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

    protected open fun setAllowedJobsForAccount(account: AccountData, supportedJobs: List<JobParameters>) {
        val allowedJobsForAccount = mutableListOf<JobParameters>()

        for (job in supportedJobs) {
            if (isJobSupported(account, job)) {
                allowedJobsForAccount.add(job)
            }
        }

        account.allowedJobs = allowedJobsForAccount
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
            mapToTanProcedureType(parameters) ?: TanProcedureType.EnterTan)
    }

    protected open fun mapToTanProcedureType(parameters: TanProcedureParameters): TanProcedureType? {
        val nameLowerCase = parameters.procedureName.toLowerCase()

        return when {
            nameLowerCase.contains("photo") -> TanProcedureType.PhotoTan

            nameLowerCase.contains("chiptan") -> {
                return when {
                    nameLowerCase.contains("qr") -> TanProcedureType.ChipTanQrCode
                    nameLowerCase.contains("manuell") -> TanProcedureType.ChipTanManuell
                    else -> TanProcedureType.ChipTanOptisch
                }
            }

            nameLowerCase.contains("push") -> return TanProcedureType.PushTan
            nameLowerCase.contains("sms") || nameLowerCase.contains("mobile") -> return TanProcedureType.SmsTan

            // TODO: what about other tan procedures we're not aware of?
            // we filter out iTAN and Einschritt-Verfahren as they are not permitted anymore according to PSD2
            else -> null
        }
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
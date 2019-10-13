package net.dankito.fints

import net.dankito.fints.messages.MessageBuilder
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemID
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatusWerte
import net.dankito.fints.model.*
import net.dankito.fints.response.InstituteSegmentId
import net.dankito.fints.response.Response
import net.dankito.fints.response.ResponseParser
import net.dankito.fints.response.client.FinTsClientResponse
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


open class FinTsClient @JvmOverloads constructor(
    protected val base64Service: IBase64Service,
    protected val webClient: IWebClient = OkHttpWebClient(),
    protected val messageBuilder: MessageBuilder = MessageBuilder(),
    protected val responseParser: ResponseParser = ResponseParser(),
    protected val threadPool: IThreadPool = ThreadPool(),
    protected val product: ProductData = ProductData("15E53C26816138699C7B6A3E8", "0.1") // TODO: get version dynamically
) {

    companion object {
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


    open fun getTransactionsAsync(parameter: GetTransactionsParameter, bank: BankData,
                                 customer: CustomerData, callback: (GetTransactionsResponse) -> Unit) {

        threadPool.runAsync {
            callback(getTransactions(parameter, bank, customer))
        }
    }

    open fun getTransactions(parameter: GetTransactionsParameter, bank: BankData,
                             customer: CustomerData): GetTransactionsResponse {

//        synchronizeCustomerSystemIdIfNotDoneYet(bank, customer) // even though specification says this is required it can be omitted


        val dialogData = DialogData()

        val initDialogResponse = initDialog(bank, customer, dialogData)

        if (initDialogResponse.successful == false) {
            return GetTransactionsResponse(initDialogResponse)
        }


        var balance: BigDecimal? = null

        if (parameter.alsoRetrieveBalance) {
            val balanceResponse = getBalanceAfterDialogInit(bank, customer, dialogData)

            if (balanceResponse.successful == false) {
                return GetTransactionsResponse(balanceResponse)
            }

            balanceResponse.getFirstSegmentById<BalanceSegment>(InstituteSegmentId.Balance)?.let {
                balance = it.balance
            }
        }


        dialogData.increaseMessageNumber()

        val requestBody = messageBuilder.createGetTransactionsMessage(parameter, bank, customer, product, dialogData)

        val response = getAndHandleResponseForMessage(requestBody, bank)

        closeDialog(bank, customer, dialogData)


        response.getFirstSegmentById<ReceivedAccountTransactions>(InstituteSegmentId.AccountTransactionsMt940)?.let { transactions ->
            return GetTransactionsResponse(response,
                transactions.bookedTransactions.sortedByDescending { it.bookingDate },
                transactions.unbookedTransactions,
                balance)
        }

        return GetTransactionsResponse(response)
    }

    protected open fun getBalanceAfterDialogInit(bank: BankData, customer: CustomerData,
                                                 dialogData: DialogData): Response {

        dialogData.increaseMessageNumber()

        val balanceRequest = messageBuilder.createGetBalanceMessage(bank, customer, product, dialogData)

        return getAndHandleResponseForMessage(balanceRequest, bank)
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

        val requestBody = messageBuilder.createBankTransferMessage(bankTransferData, bank, customer, dialogData)

        val response = getAndHandleResponseForMessage(requestBody, bank)

        closeDialog(bank, customer, dialogData)

        return FinTsClientResponse(response)
    }


    protected open fun initDialog(bank: BankData, customer: CustomerData, dialogData: DialogData): Response {

        val requestBody = messageBuilder.createInitDialogMessage(bank, customer, product, dialogData)

        val response = getAndHandleResponseForMessage(requestBody, bank)

        if (response.successful) {
            updateBankData(bank, response)
            updateCustomerData(customer, response)

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

        return FinTsClientResponse(true, false)
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
            updateCustomerData(customer, response)

            response.messageHeader?.let { header -> dialogData.dialogId = header.dialogId }

            closeDialog(bank, customer, dialogData)
        }

        return FinTsClientResponse(response)
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
//            bank.finTs3ServerAddress =  // TODO: parse HIKOM
        }
    }

    protected open fun updateCustomerData(customer: CustomerData, response: Response) {
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

        response.getFirstSegmentById<UserParameters>(InstituteSegmentId.UserParameters)?.let { userParameters ->
            customer.updVersion = userParameters.updVersion

            if (customer.name.isEmpty()) {
                userParameters.username?.let {
                    customer.name = it
                }
            }

            // TODO: may also make use of other info
        }

        val allowedJobsForBank = response.allowedJobs
        if (allowedJobsForBank.isNotEmpty()) { // if allowedJobsForBank is empty than bank didn't send any allowed job
            for (account in customer.accounts) {
                val allowedJobsForAccount = mutableListOf<AllowedJob>()

                for (job in allowedJobsForBank) {
                    if (isJobSupported(account, job)) {
                        allowedJobsForAccount.add(job)
                    }
                }

                account.allowedJobs = allowedJobsForAccount
            }
        }
    }

    protected open fun isJobSupported(account: AccountData, job: AllowedJob): Boolean {
        for (allowedJobName in account.allowedJobNames) {
            if (allowedJobName == job.jobName) {
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
package net.dankito.fints

import net.dankito.fints.messages.MessageBuilder
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatusWerte
import net.dankito.fints.model.*
import net.dankito.fints.response.InstituteSegmentId
import net.dankito.fints.response.Response
import net.dankito.fints.response.ResponseParser
import net.dankito.fints.response.segments.AccountInfo
import net.dankito.fints.response.segments.BankParameters
import net.dankito.fints.response.segments.ReceivedSynchronization
import net.dankito.fints.response.segments.UserParameters
import net.dankito.fints.util.IBase64Service
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.OkHttpWebClient
import net.dankito.utils.web.client.RequestParameters
import net.dankito.utils.web.client.WebClientResponse
import org.slf4j.LoggerFactory


open class FinTsClient(
    protected val base64Service: IBase64Service,
    protected val webClient: IWebClient = OkHttpWebClient(),
    protected val messageBuilder: MessageBuilder = MessageBuilder(),
    protected val responseParser: ResponseParser = ResponseParser()
) {

    companion object {
        private val log = LoggerFactory.getLogger(FinTsClient::class.java)
    }


    open fun getAnonymousBankInfo(bank: BankData, product: ProductData): Response {
        val dialogData = DialogData()

        val requestBody = messageBuilder.createAnonymousDialogInitMessage(bank, product, dialogData)

        val response = getAndHandleResponseForMessage(requestBody, bank)

        if (response.successful) {
            updateBankData(bank, response)

            dialogData.increaseMessageNumber()
            response.messageHeader?.let { header -> dialogData.dialogId = header.dialogId }

            val dialogEndRequestBody = messageBuilder.createAnonymousDialogEndMessage(bank, dialogData)

            getResponseForMessage(dialogEndRequestBody, bank)
        }

        return response
    }


    open fun initDialog(bank: BankData, customer: CustomerData, product: ProductData,
                                         dialogData: DialogData): Response {

        val requestBody = messageBuilder.createInitDialogMessage(bank, customer, product, dialogData)

        val response = getAndHandleResponseForMessage(requestBody, bank)

        if (response.successful) {
            updateBankData(bank, response)
            updateCustomerData(customer, response)

            response.messageHeader?.let { header -> dialogData.dialogId = header.dialogId }
        }

        return response
    }

    open fun synchronizeCustomerSystemId(bank: BankData, customer: CustomerData, product: ProductData,
                                         dialogData: DialogData = DialogData()): Response {

        val requestBody = messageBuilder.createSynchronizeCustomerSystemIdMessage(bank, customer, product, dialogData)

        val response = getAndHandleResponseForMessage(requestBody, bank)

        if (response.successful) {
            updateBankData(bank, response)
            updateCustomerData(customer, response)

            response.messageHeader?.let { header -> dialogData.dialogId = header.dialogId }

            closeDialog(bank, customer, dialogData)
        }

        return response
    }


    open fun getTransactions(parameter: GetTransactionsParameter, bank: BankData, customer: CustomerData,
                             product: ProductData): Response {

        val dialogData = DialogData()

        val initDialogResponse = initDialog(bank, customer, product, dialogData)

        if (initDialogResponse.successful == false) {
            return initDialogResponse
        }


        dialogData.increaseMessageNumber()

        val balanceRequest = messageBuilder.createGetBalanceMessage(bank, customer, product, dialogData)

        val balanceResponse = getAndHandleResponseForMessage(balanceRequest, bank)

        if (balanceResponse.successful == false) {
            return balanceResponse
        }


        dialogData.increaseMessageNumber()

        val requestBody = messageBuilder.createGetTransactionsMessage(parameter, bank, customer, product, dialogData)

        val response = getAndHandleResponseForMessage(requestBody, bank)

        closeDialog(bank, customer, dialogData)

        return response
    }


    open fun doBankTransfer(bankTransferData: BankTransferData, bank: BankData, customer: CustomerData, product: ProductData): Response {
        val dialogData = DialogData()

        val initDialogResponse = initDialog(bank, customer, product, dialogData)

        if (initDialogResponse.successful == false) {
            return initDialogResponse
        }


        dialogData.increaseMessageNumber()

        val requestBody = messageBuilder.createBankTransferMessage(bankTransferData, bank, customer, dialogData)

        val response = getAndHandleResponseForMessage(requestBody, bank)

        closeDialog(bank, customer, dialogData)

        return response
    }


    protected open fun closeDialog(bank: BankData, customer: CustomerData, dialogData: DialogData) {
        dialogData.increaseMessageNumber()

        val dialogEndRequestBody = messageBuilder.createDialogEndMessage(bank, customer, dialogData)

        getResponseForMessage(dialogEndRequestBody, bank)
    }


    protected open fun getAndHandleResponseForMessage(requestBody: String, bank: BankData): Response {
        val webResponse = getResponseForMessage(requestBody, bank)

        return handleResponse(webResponse)
    }

    protected open fun getResponseForMessage(requestBody: String, bank: BankData): WebClientResponse {
        log.debug("Sending message:\n$requestBody")

        val encodedRequestBody = base64Service.encode(requestBody)

        return webClient.post(
            RequestParameters(bank.finTs3ServerAddress, encodedRequestBody, "application/octet-stream")
        )
    }

    protected open fun handleResponse(webResponse: WebClientResponse): Response {
        val responseBody = webResponse.body

        if (webResponse.isSuccessful && responseBody != null) {

            val decodedResponse = decodeBase64Response(responseBody)

            log.debug("Received message:\n$decodedResponse")

            return responseParser.parse(decodedResponse)
        }

        return Response(false, false, error = webResponse.error)
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

            // TODO: may also save securityReferenceNumbers
        }

        response.getFirstSegmentById<AccountInfo>(InstituteSegmentId.AccountInfo)?.let { accountInfo ->
            customer.iban = accountInfo.iban
            customer.name = accountInfo.accountHolderName1

            // TODO: may also make use of other info
        }

        response.getFirstSegmentById<UserParameters>(InstituteSegmentId.UserParameters)?.let { userParameters ->
            customer.updVersion = userParameters.updVersion

            // TODO: may also make use of other info
        }
    }

}
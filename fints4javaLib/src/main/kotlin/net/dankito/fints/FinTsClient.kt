package net.dankito.fints

import net.dankito.fints.messages.MessageBuilder
import net.dankito.fints.model.BankData
import net.dankito.fints.model.CustomerData
import net.dankito.fints.model.DialogData
import net.dankito.fints.model.ProductData
import net.dankito.fints.response.InstituteSegmentId
import net.dankito.fints.response.Response
import net.dankito.fints.response.ResponseParser
import net.dankito.fints.response.segments.ReceivedSynchronization
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
            dialogData.increaseMessageNumber()
            response.messageHeader?.let { header -> dialogData.dialogId = header.dialogId }

            val dialogEndRequestBody = messageBuilder.createAnonymousDialogEndMessage(bank, dialogData)

            getResponseForMessage(dialogEndRequestBody, bank)
        }

        return response
    }


    open fun synchronizeCustomerSystemId(bank: BankData, customer: CustomerData, product: ProductData,
                                         dialogData: DialogData = DialogData()): Response {

        val requestBody = messageBuilder.createSynchronizeCustomerSystemIdMessage(bank, customer, product, dialogData)

        val response = getAndHandleResponseForMessage(requestBody, bank)

        if (response.successful) {
            response.messageHeader?.let { header -> dialogData.dialogId = header.dialogId }

            response.getFirstSegmentById<ReceivedSynchronization>(InstituteSegmentId.Synchronization)?.customerSystemId?.let { customerSystemId ->
                customer.customerSystemId = customerSystemId
            }

            closeDialog(bank, customer, dialogData)
        }

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

}
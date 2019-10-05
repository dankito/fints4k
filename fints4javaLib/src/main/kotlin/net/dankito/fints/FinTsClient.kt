package net.dankito.fints

import net.dankito.fints.messages.MessageBuilder
import net.dankito.fints.model.BankData
import net.dankito.fints.model.CustomerData
import net.dankito.fints.model.ProductData
import net.dankito.fints.util.IBase64Service
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.OkHttpWebClient
import net.dankito.utils.web.client.RequestParameters
import net.dankito.utils.web.client.WebClientResponse


open class FinTsClient(
    protected val base64Service: IBase64Service,
    protected val webClient: IWebClient = OkHttpWebClient(),
    protected val messageBuilder: MessageBuilder = MessageBuilder()
) {


    fun getAnonymousBankInfo(bank: BankData, product: ProductData) {
        val requestBody = messageBuilder.createAnonymousDialogInitMessage(bank, product)

        val response = getResponseForMessage(requestBody, bank)

        handleResponse(response)
    }

    fun getBankInfo(bank: BankData, customer: CustomerData, product: ProductData) {
        val requestBody = messageBuilder.createDialogInitMessage(bank, customer, product)

        val response = getResponseForMessage(requestBody, bank)

        handleResponse(response)
    }


    protected open fun getResponseForMessage(requestBody: String, bank: BankData): WebClientResponse {
        val encodedRequestBody = base64Service.encode(requestBody)

        return webClient.post(
            RequestParameters(bank.finTs3ServerAddress, encodedRequestBody, "application/octet-stream")
        )
    }

    protected open fun handleResponse(response: WebClientResponse) {
        val responseBody = response.body

        if (response.isSuccessful && responseBody != null) {

            val decodedResponse = decodeBase64Response(responseBody)

            if (decodedResponse != null) {

            }
        }
    }

    protected open fun decodeBase64Response(responseBody: String): String {
        return base64Service.decode(responseBody.replace("\r", "").replace("\n", ""))
    }

}
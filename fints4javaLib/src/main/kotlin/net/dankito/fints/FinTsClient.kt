package net.dankito.fints

import net.dankito.fints.messages.MessageBuilder
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemID
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatusWerte
import net.dankito.fints.model.AccountCredentials
import net.dankito.fints.model.BankInfo
import net.dankito.fints.model.ProductInfo
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


    fun getAnonymousBankInfo(bankInfo: BankInfo, productInfo: ProductInfo) {
        val requestBody = messageBuilder.createAnonymousDialogInitMessage(bankInfo.countryCode, bankInfo.bankCode,
            productInfo.productName, productInfo.productVersion)

        val response = getResponseForMessage(requestBody, bankInfo)

        handleResponse(response)
    }

    fun getBankInfo(credentials: AccountCredentials, bankInfo: BankInfo, productInfo: ProductInfo) {
        val requestBody = messageBuilder.createDialogInitMessage(bankInfo.countryCode, bankInfo.bankCode,
            credentials.customerId, KundensystemID.PinTan, KundensystemStatusWerte.Benoetigt, 0, 0, Dialogsprache.German,
            productInfo.productName, productInfo.productVersion)

        val response = getResponseForMessage(requestBody, bankInfo)

        handleResponse(response)
    }


    protected open fun getResponseForMessage(requestBody: String, bankInfo: BankInfo): WebClientResponse {
        val encodedRequestBody = base64Service.encode(requestBody)

        return webClient.post(
            RequestParameters(bankInfo.finTsServerAddress, encodedRequestBody, "application/octet-stream")
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
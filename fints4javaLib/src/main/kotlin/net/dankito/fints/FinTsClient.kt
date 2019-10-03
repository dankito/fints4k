package net.dankito.fints

import net.dankito.fints.messages.nachrichten.implementierte.DialoginitialisierungAnonym
import net.dankito.fints.model.BankInfo
import net.dankito.fints.model.ProductInfo
import net.dankito.fints.util.IBase64Service
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.OkHttpWebClient
import net.dankito.utils.web.client.RequestParameters


open class FinTsClient(
    protected val base64Service: IBase64Service,
    protected val webClient: IWebClient = OkHttpWebClient()
) {


    fun getBankInfo(bankInfo: BankInfo, productInfo: ProductInfo) {
        val dialogInit = DialoginitialisierungAnonym(bankInfo.countryCode, bankInfo.bankCode,
            productInfo.productName, productInfo.productVersion)

        val requestBody = dialogInit.format()
        val encodedRequestBody = base64Service.encode(requestBody)

        val response = webClient.post(RequestParameters(bankInfo.finTsServerAddress, encodedRequestBody, "application/octet-stream"))

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
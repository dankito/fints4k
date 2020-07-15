package net.dankito.banking.fints.webclient

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import net.dankito.utils.multiplatform.log.LoggerFactory


open class KtorWebClient : IWebClient {

    companion object {
        private val log = LoggerFactory.getLogger(KtorWebClient::class)
    }


    protected val client = HttpClient() {

    }


    open fun close() {
        try {
            client.close()
        } catch (e: Exception) {
            // Cancel after timeout
            client.cancel()
        }
    }


    override fun post(url: String, body: String, contentType: String, userAgent: String, callback: (WebClientResponse) -> Unit) {
        GlobalScope.async {
            try {
                val clientResponse = client.post<HttpResponse>(url) {
                    this.body = TextContent(body, contentType = ContentType.Application.OctetStream)
                }

                val responseBody = clientResponse.readText()

                callback(WebClientResponse(clientResponse.status.value == 200, clientResponse.status.value, body = responseBody))
            } catch (e: Exception) {
                log.error(e) { "Could not send request to url '$url'" }

                callback(WebClientResponse(false, error = e))
            }
        }
    }

}
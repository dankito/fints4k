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


open class KtorWebClient : IWebClient {

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


    override fun post(url: String, body: String, contentType: String, userAgent: String): WebClientResponse {
        try {
            val job = GlobalScope.async {
                val clientResponse = client.post<HttpResponse>(url) {
                    this.body = TextContent(body, contentType = ContentType.Application.OctetStream)
                }

                val responseBody = clientResponse.readText()

                WebClientResponse(clientResponse.status.value == 200, clientResponse.status.value, body = responseBody)
            }

            while (job.isCompleted == false) { } // let's warm the CPU to get suspend function synchronous (runBlocking is not available in common projects)

            return job.getCompleted()
        } catch (e: Exception) {
            return WebClientResponse(false, error = e)
        }
    }
}
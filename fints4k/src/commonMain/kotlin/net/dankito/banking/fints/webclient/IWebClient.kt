package net.dankito.banking.fints.webclient


interface IWebClient {

    companion object {
        const val DefaultUserAgent = "Mozilla/5.0 (Windows NT 6.3; rv:55.0) Gecko/20100101 Firefox/55.0"

        const val DefaultMobileUserAgent = "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/537.36 (KHTML, like Gecko) CChrome/60.0.3112.105 Safari/537.36"

        const val DefaultConnectionTimeoutMillis = 2000

        const val DefaultReadTimeoutMillis = 15000

        const val DefaultWriteTimeoutMillis = 30000

        const val DefaultDownloadBufferSize = 8 * 1024

        const val DefaultCountConnectionRetries = 2
    }


    fun post(url: String, body: String, contentType: String = "application/octet-stream", userAgent: String = DefaultUserAgent, callback: (WebClientResponse) -> Unit)

}
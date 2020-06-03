package net.dankito.banking.fints.webclient


open class WebClientResponse(
    val successful: Boolean,
    val responseCode: Int = -1,
    val error: Exception? = null,
    val body: String? = null
) {


    open val isInformationalResponse: Boolean
        get() = responseCode >= 100 && responseCode < 200

    open val isSuccessResponse: Boolean
        get() = responseCode >= 200 && responseCode < 300

    open val isRedirectionResponse: Boolean
        get() = responseCode >= 300 && responseCode < 400

    open val isClientErrorResponse: Boolean
        get() = responseCode >= 400 && responseCode < 500

    open val isServerErrorResponse: Boolean
        get() = responseCode >= 500 && responseCode < 600


    override fun toString(): String {
        if (successful) {
            return "Successful: $responseCode"
        }

        return "Error: $responseCode $error"
    }

}
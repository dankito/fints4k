package net.dankito.banking.ui.android.authentication


open class AuthenticationResult(
    open val successful: Boolean,
    open val error: String? =  null
) {

    override fun toString(): String {
        return if (successful) {
            "Successful"
        }
        else {
            "Error occurred: $error"
        }
    }

}
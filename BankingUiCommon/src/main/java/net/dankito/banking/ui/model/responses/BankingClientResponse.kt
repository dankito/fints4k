package net.dankito.banking.ui.model.responses


open class BankingClientResponse(
    val isSuccessful: Boolean,
    val errorToShowToUser: String?,
    val error: Exception? = null
) {


    override fun toString(): String {
        return if (isSuccessful) {
            "Successful"
        }
        else {
            "Error: $errorToShowToUser"
        }
    }

}
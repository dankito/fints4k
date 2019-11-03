package net.dankito.banking.ui.model.responses


open class BankingClientResponse(
    val isSuccessful: Boolean,
    val errorToShowToUser: String?
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
package net.dankito.banking.ui.model.responses


open class BankingClientResponse(
    val isSuccessful: Boolean,
    val errorToShowToUser: String?,
    val userCancelledAction: Boolean = false // TODO: not implemented in hbci4jBankingClient yet
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
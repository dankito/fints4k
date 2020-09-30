package net.dankito.banking.ui.model.responses


open class BankingClientResponse(
    open val successful: Boolean,
    open val errorToShowToUser: String?,
    open val wrongCredentialsEntered: Boolean = false,
    open val userCancelledAction: Boolean = false // TODO: not implemented in hbci4jBankingClient yet
) {


    override fun toString(): String {
        return if (successful) {
            "Successful"
        }
        else {
            "Error: $errorToShowToUser"
        }
    }

}
package net.dankito.banking.ui.model.responses


open class BankingClientResponse(
    open val isSuccessful: Boolean,
    open val errorToShowToUser: String?,
    open val userCancelledAction: Boolean = false // TODO: not implemented in hbci4jBankingClient yet
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
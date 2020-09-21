package net.dankito.banking.ui.model.tan


open class TanChallenge(
    val messageToShowToUser: String,
    val tanMethod: TanMethod
) {

    override fun toString(): String {
        return "$tanMethod: $messageToShowToUser"
    }

}
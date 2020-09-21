package net.dankito.banking.fints.model


open class TanChallenge(
    val messageToShowToUser: String,
    val challenge: String,
    val tanMethod: TanMethod,
    val tanMediaIdentifier: String?
) {

    override fun toString(): String {
        return "$tanMethod (medium: $tanMediaIdentifier): $messageToShowToUser"
    }

}
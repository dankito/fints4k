package net.dankito.banking.fints.model


open class TanChallenge(
    val messageToShowToUser: String,
    val challenge: String,
    val tanProcedure: TanProcedure,
    val tanMediaIdentifier: String?
) {

    override fun toString(): String {
        return "$tanProcedure (medium: $tanMediaIdentifier): $messageToShowToUser"
    }

}
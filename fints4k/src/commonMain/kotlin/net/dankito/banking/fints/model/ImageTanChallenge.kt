package net.dankito.banking.fints.model

import net.dankito.banking.fints.tan.TanImage


open class ImageTanChallenge(
    val image: TanImage,
    messageToShowToUser: String,
    challenge: String,
    tanProcedure: TanProcedure,
    tanMediaIdentifier: String?
) : TanChallenge(messageToShowToUser, challenge, tanProcedure, tanMediaIdentifier) {

    override fun toString(): String {
        return "$tanProcedure (medium: $tanMediaIdentifier) $image: $messageToShowToUser"
    }

}
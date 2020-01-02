package net.dankito.fints.model

import net.dankito.fints.tan.TanImage


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
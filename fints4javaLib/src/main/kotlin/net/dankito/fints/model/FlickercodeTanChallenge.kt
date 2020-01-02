package net.dankito.fints.model

import net.dankito.fints.tan.Flickercode


open class FlickercodeTanChallenge(
    val flickercode: Flickercode,
    messageToShowToUser: String,
    challenge: String,
    tanProcedure: TanProcedure,
    tanMediaIdentifier: String?
) : TanChallenge(messageToShowToUser, challenge, tanProcedure, tanMediaIdentifier) {

    override fun toString(): String {
        return "$tanProcedure (medium: $tanMediaIdentifier) $flickercode: $messageToShowToUser"
    }

}
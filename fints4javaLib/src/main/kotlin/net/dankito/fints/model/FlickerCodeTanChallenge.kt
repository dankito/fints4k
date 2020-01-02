package net.dankito.fints.model

import net.dankito.fints.tan.FlickerCode


open class FlickerCodeTanChallenge(
    val flickerCode: FlickerCode,
    messageToShowToUser: String,
    challenge: String,
    tanProcedure: TanProcedure,
    tanMediaIdentifier: String?
) : TanChallenge(messageToShowToUser, challenge, tanProcedure, tanMediaIdentifier) {

    override fun toString(): String {
        return "$tanProcedure (medium: $tanMediaIdentifier) $flickerCode: $messageToShowToUser"
    }

}
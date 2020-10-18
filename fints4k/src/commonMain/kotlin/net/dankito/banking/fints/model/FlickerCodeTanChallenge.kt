package net.dankito.banking.fints.model

import net.dankito.banking.fints.tan.FlickerCode


open class FlickerCodeTanChallenge(
    val flickerCode: FlickerCode,
    messageToShowToUser: String,
    challenge: String,
    tanMethod: TanMethod,
    tanMediaIdentifier: String?
) : TanChallenge(messageToShowToUser, challenge, tanMethod, tanMediaIdentifier) {

    override fun toString(): String {
        return "$tanMethod (medium: $tanMediaIdentifier) $flickerCode: $messageToShowToUser"
    }

}
package net.dankito.banking.fints.model

import net.dankito.banking.fints.tan.TanImage


open class ImageTanChallenge(
    val image: TanImage,
    messageToShowToUser: String,
    challenge: String,
    tanMethod: TanMethod,
    tanMediaIdentifier: String?
) : TanChallenge(messageToShowToUser, challenge, tanMethod, tanMediaIdentifier) {

    override fun toString(): String {
        return "$tanMethod (medium: $tanMediaIdentifier) $image: $messageToShowToUser"
    }

}
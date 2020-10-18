package net.dankito.banking.ui.model.tan


open class ImageTanChallenge(
    val image: TanImage,
    messageToShowToUser: String,
    tanMethod: TanMethod

    ) : TanChallenge(messageToShowToUser, tanMethod) {

    override fun toString(): String {
        return "$tanMethod $image: $messageToShowToUser"
    }

}
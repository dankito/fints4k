package net.dankito.banking.ui.model.tan


open class ImageTanChallenge(
    val image: TanImage,
    messageToShowToUser: String,
    tanProcedure: TanProcedure

    ) : TanChallenge(messageToShowToUser, tanProcedure) {

    override fun toString(): String {
        return "$tanProcedure $image: $messageToShowToUser"
    }

}
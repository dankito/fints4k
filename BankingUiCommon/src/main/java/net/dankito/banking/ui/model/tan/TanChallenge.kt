package net.dankito.banking.ui.model.tan


open class TanChallenge(
    val messageToShowToUser: String,
    val tanProcedure: TanProcedure
) {

    override fun toString(): String {
        return "$tanProcedure: $messageToShowToUser"
    }

}
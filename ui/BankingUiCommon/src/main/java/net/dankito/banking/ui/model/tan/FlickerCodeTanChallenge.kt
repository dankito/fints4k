package net.dankito.banking.ui.model.tan


open class FlickerCodeTanChallenge(
    val flickerCode: FlickerCode,
    messageToShowToUser: String,
    tanProcedure: TanProcedure

) : TanChallenge(messageToShowToUser, tanProcedure) {

    override fun toString(): String {
        return "$tanProcedure $flickerCode: $messageToShowToUser"
    }

}
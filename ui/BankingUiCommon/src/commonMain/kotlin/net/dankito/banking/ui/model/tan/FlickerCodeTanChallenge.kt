package net.dankito.banking.ui.model.tan


open class FlickerCodeTanChallenge(
    val flickerCode: FlickerCode,
    messageToShowToUser: String,
    tanMethod: TanMethod

) : TanChallenge(messageToShowToUser, tanMethod) {

    override fun toString(): String {
        return "$tanMethod $flickerCode: $messageToShowToUser"
    }

}
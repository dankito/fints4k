package net.dankito.banking.ui.model.tan


open class TanGeneratorTanMedium(
    displayName: String,
    status: TanMediumStatus,
    val cardNumber: String

) : TanMedium(displayName, status) {

    override fun toString(): String {
        return "$displayName $status"
    }

}
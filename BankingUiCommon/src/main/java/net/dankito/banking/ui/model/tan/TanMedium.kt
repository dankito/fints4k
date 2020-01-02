package net.dankito.banking.ui.model.tan


open class TanMedium(
    val displayName: String,
    val status: TanMediumStatus
) {

    override fun toString(): String {
        return "$displayName $status"
    }

}
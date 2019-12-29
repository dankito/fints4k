package net.dankito.banking.ui.model


open class TanMedium(
    val displayName: String,
    val status: TanMediumStatus,
    val originalObject: Any
) {

    override fun toString(): String {
        return "$displayName $status"
    }

}
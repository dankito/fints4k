package net.dankito.banking.ui.model.tan


open class TanMedium(
    val displayName: String,
    val status: TanMediumStatus
) {


    internal constructor() : this("", TanMediumStatus.Available) // for object deserializers


    override fun toString(): String {
        return "$displayName $status"
    }

}
package net.dankito.banking.ui.model.tan

import net.dankito.banking.ui.model.Displayable


open class TanMedium(
    override val displayName: String,
    val status: TanMediumStatus
) : Displayable {


    internal constructor() : this("", TanMediumStatus.Available) // for object deserializers


    override fun toString(): String {
        return "$displayName $status"
    }

}
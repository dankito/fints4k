package net.dankito.banking.ui.model.tan

import net.dankito.banking.ui.model.Displayable
import net.dankito.utils.multiplatform.UUID


open class TanMedium(
    override val displayName: String,
    val status: TanMediumStatus
) : Displayable {


    internal constructor() : this("", TanMediumStatus.Available) // for object deserializers


    open var technicalId: String = UUID.random()


    override fun toString(): String {
        return "$displayName $status"
    }

}
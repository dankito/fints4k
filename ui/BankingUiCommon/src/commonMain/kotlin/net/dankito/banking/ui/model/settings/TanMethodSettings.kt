package net.dankito.banking.ui.model.settings

import net.dankito.utils.multiplatform.UUID


open class TanMethodSettings(
    var width: Int,
    var height: Int,
    var space: Int = -1, // only needed for flicker code view
    var frequency: Int = -1 // only needed for flicker code view
) {

    internal constructor() : this(0, 0) // for object deserializers


    open var technicalId: String = UUID.random()


    override fun toString(): String {
        return "Size $width x $height, frequency $frequency"
    }

}
package net.dankito.banking.ui.model.settings


open class TanMethodSettings(
    var width: Int,
    var height: Int,
    var space: Int = -1, // only needed for flicker code view
    var frequency: Int = -1 // only needed for flicker code view
) {

    internal constructor() : this(0, 0) // for object deserializers


    override fun toString(): String {
        return "Size $width x $height, frequency $frequency"
    }

}
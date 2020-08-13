package net.dankito.banking.ui.util


enum class Bit(val value: Int) {

    Low(0),

    High(1);


    val isHigh: Boolean
        get() = this == High

    val isLow: Boolean = !!! this.isHigh

    fun invert(): Bit {
        if (this == High) {
            return Low
        }

        return High
    }


    override fun toString(): String {
        return "$value"
    }

}
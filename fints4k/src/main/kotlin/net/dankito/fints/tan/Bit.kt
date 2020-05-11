package net.dankito.fints.tan


enum class Bit(val value: Int) {

    Low(0),

    High(1);


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
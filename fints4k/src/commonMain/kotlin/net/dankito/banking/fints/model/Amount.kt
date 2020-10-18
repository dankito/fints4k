package net.dankito.banking.fints.model

import net.dankito.utils.multiplatform.BigDecimal


open class Amount(
    val string: String
) {

    companion object {
        val Zero = Amount("0,")
    }


    internal constructor() : this("") // for object deserializers


    open val bigDecimal: BigDecimal
        get() = BigDecimal(this.string.replace(',', '.'))


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Amount) return false

        if (string != other.string) return false

        return true
    }

    override fun hashCode(): Int {
        return string.hashCode()
    }


    override fun toString(): String {
        return string
    }

}
package net.dankito.banking.fints.model


open class Currency(
    val code: String
) {

    internal constructor() : this("") // for object deserializers


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Currency) return false

        if (code != other.code) return false

        return true
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }


    override fun toString(): String {
        return code
    }

}
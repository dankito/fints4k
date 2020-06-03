package net.dankito.banking.fints.model


open class Currency(
    val code: String
) {

    internal constructor() : this("") // for object deserializers


    override fun toString(): String {
        return code
    }

}
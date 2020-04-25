package net.dankito.banking.search


data class Remittee(
    val name: String,
    val iban: String,
    val bic: String
) {


    internal constructor() : this("", "", "") // for object deserializers


    override fun toString(): String {
        return name
    }

}
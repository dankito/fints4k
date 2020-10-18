package net.dankito.banking.search


data class TransactionParty(
    val name: String,
    val iban: String?,
    val bic: String?,
    var bankName: String? = null
) {


    internal constructor() : this("", "", "") // for object deserializers


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionParty) return false

        if (name.equals(other.name, true) == false) return false
        if (iban != other.iban) return false
        if (bic != other.bic) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.toLowerCase().hashCode()
        result = 31 * result + (iban?.hashCode() ?: 0)
        result = 31 * result + (bic?.hashCode() ?: 0)
        return result
    }


    override fun toString(): String {
        return name
    }

}
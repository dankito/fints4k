package net.dankito.banking.banklistcreator.parser.model


open class BankCodeListEntry(
    val bankName: String,
    val bankCode: String,
    var bic: String, // TODO: make val again
    val postalCode: String,
    val city: String,
    val checksumMethod: String,
    val oldBankCode: String?
) {

    val isBankCodeDeleted: Boolean
        get() = oldBankCode != null


    override fun toString(): String {
        return "$bankCode $bankName ($bic, $city)"
    }

}
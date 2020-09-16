package net.dankito.banking.banklistcreator.parser.model


open class BankCodeListEntry(
    open val bankName: String,
    open val bankCode: String,
    open var bic: String, // TODO: make val again
    open val postalCode: String,
    open val city: String,
    open val checksumMethod: String,
    open val oldBankCode: String?
) {

    open val isBankCodeDeleted: Boolean
        get() = oldBankCode != null


    override fun toString(): String {
        return "$bankCode $bankName ($bic, $city)"
    }

}
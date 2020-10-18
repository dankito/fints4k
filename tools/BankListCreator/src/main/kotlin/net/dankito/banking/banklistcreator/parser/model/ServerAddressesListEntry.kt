package net.dankito.banking.banklistcreator.parser.model


open class ServerAddressesListEntry(
    open val bankName: String,
    open val bankCode: String,
    open val bic: String,
    open val city: String,
    open val pinTanAddress: String?,
    open val pinTanVersion: String?
) {

    override fun toString(): String {
        return "$bankCode $bankName ($city, $pinTanAddress)"
    }

}
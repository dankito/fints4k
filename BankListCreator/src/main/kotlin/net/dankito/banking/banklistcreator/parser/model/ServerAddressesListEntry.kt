package net.dankito.banking.banklistcreator.parser.model


open class ServerAddressesListEntry(
    val bankName: String,
    val bankCode: String,
    val bic: String,
    val city: String,
    val pinTanAddress: String?,
    val pinTanVersion: String?
) {

    override fun toString(): String {
        return "$bankCode $bankName ($city, $pinTanAddress)"
    }

}
package net.dankito.banking.fints.model


open class BankInfo(
    val name: String,
    val bankCode: String,
    val bic: String,
    val postalCode: String,
    val city: String,
    val checksumMethod: String,
    val pinTanAddress: String?,
    val pinTanVersion: String?,
    val oldBankCode: String?
) {


    protected constructor() : this("", "", "", "", "", "", null, null, null) // for object deserializers

    val supportsPinTan: Boolean
        get() = pinTanAddress.isNullOrEmpty() == false

    val supportsFinTs3_0: Boolean
        get() = pinTanVersion == "FinTS V3.0"

    val isBankCodeDeleted: Boolean
        get() = oldBankCode != null // TODO: this is not in all cases true, there are banks with new bank code which haven't been deleted


    override fun toString(): String {
        return "$bankCode $name $city"
    }

}
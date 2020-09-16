package net.dankito.banking.bankfinder


open class BankInfo constructor(
    open val name: String,
    open val bankCode: String,
    open val bic: String,
    open val postalCode: String,
    open val city: String,
    open val pinTanAddress: String?,
    open val pinTanVersion: String?
) {

    protected constructor() : this("", "", "", "", "", null, "") // for object deserializers


    open val supportsPinTan: Boolean
        get() = pinTanAddress.isNullOrEmpty() == false

    open val supportsFinTs3_0: Boolean
        get() = pinTanVersion == "FinTS V3.0"


    override fun toString(): String {
        return "$bankCode $name $city"
    }

}
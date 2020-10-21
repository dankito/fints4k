package net.codinux.banking.tools.epcqrcode


open class EpcQrCode(

    /**
     * 3 bytes.
     * Always(?) has value "BCD".
     */
    open val serviceTag: String,

    /**
     * 3 bytes.
     * Has either value "001" or "002".
     */
    open val version: EpcQrCodeVersion,

    /**
     * 1 byte.
     * The values 1,2,3,4,5,6,7 and 8 determine the interpretation of data to be used.
     * In that order they qualify UTF-8, ISO 8895-1, ISO 8895-2, ISO 8895-4, ISO 8895-5, ISO 8895- 7, ISO 8895-10 and ISO 8895-15
     */
    open val coding: EpcQrCodeCharacterSet,

    /**
     * 3 bytes.
     * The Function is defined by its key value: SCT - SEPA Credit Transfer
     */
    open val function: String,

    /**
     * Receiver's BIC.
     * Mandatory in Version 001, optional in Version 002.
     * Either 8 or 11 bytes.
     */
    open val bic: String?,

    /**
     * Receiver name.
     * Max. 70 characters
     */
    open val receiverName: String,

    /**
     * Receiver's IBAN.
     * Max. 34 bytes.
     */
    open val iban: String,

    /**
     * Three capital letter currency code.
     * Only set if amount is also set.
     */
    open val currencyCode: String?,

    /**
     * Optional amount.
     * Max. 12 bytes.
     */
    open val amount: Double?, // TODO: use BigDecimal

    /**
     * Optional purpose code.
     * Max. 4 bytes.
     */
    open val purposeCode: String?,

    open val remittanceReference: String?,

    open val remittanceText: String?,

    open val originatorInformation: String?

) {

    /**
     * [remittanceReference] and [remittanceText] are mutual exclusive, that means one of both has to be set
     * but they are never set both at the same time.
     *
     * remittance returns the one that is set.
     */
    open val remittance: String
        get() = remittanceReference ?: remittanceText ?: "" // they should never be both null

    override fun toString(): String {
        return "$receiverName $amount $currencyCode ${remittance}"
    }
}
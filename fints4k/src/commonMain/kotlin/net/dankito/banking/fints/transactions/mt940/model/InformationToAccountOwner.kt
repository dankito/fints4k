package net.dankito.banking.fints.transactions.mt940.model


open class InformationToAccountOwner(
    val unparsedReference: String,
    val otherPartyName: String?,
    val otherPartyBankCode: String?,
    val otherPartyAccountId: String?,
    val bookingText: String?,
    val primaNotaNumber: String?,
    val textKeySupplement: String?
) {

    var endToEndReference: String? = null

    var customerReference: String? = null

    var mandateReference: String? = null

    var creditorIdentifier: String? = null

    var originatorsIdentificationCode: String? = null

    var compensationAmount: String? = null

    var originalAmount: String? = null

    var sepaReference: String? = null

    var deviantOriginator: String? = null

    var deviantRecipient: String? = null

    var referenceWithNoSpecialType: String? = null


    override fun toString(): String {
        return "$otherPartyName $unparsedReference"
    }

}
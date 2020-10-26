package net.codinux.banking.tools.importerexporter.model

import java.math.BigDecimal
import java.util.*


open class AccountTransaction(
    open val account: String,
    open val amount: BigDecimal,
    open val currency: String,
    open val reference: String,
    open val bookingDate: Date,
    open val valueDate: Date,
    open val otherPartyName: String?,
    open val otherPartyBankCode: String?,
    open val otherPartyAccountId: String?,
    open val bookingText: String?
) {

}
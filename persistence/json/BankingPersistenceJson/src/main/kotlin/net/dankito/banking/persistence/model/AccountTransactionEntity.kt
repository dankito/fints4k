package net.dankito.banking.persistence.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.UUID


@JsonIdentityInfo(property = "id", generator = ObjectIdGenerators.PropertyGenerator::class) // to avoid stack overflow due to circular references
// had to define all properties as 'var' 'cause MapStruct cannot handle vals
open class AccountTransactionEntity(
    open var bankAccount: BankAccountEntity,
    open var amount: BigDecimal,
    open var currency: String,
    open var unparsedUsage: String,
    open var bookingDate: Date,
    open var otherPartyName: String?,
    open var otherPartyBankCode: String?,
    open var otherPartyAccountId: String?,
    open var bookingText: String?,
    open var valueDate: Date,
    open var statementNumber: Int,
    open var sequenceNumber: Int?,
    open var openingBalance: BigDecimal?,
    open var closingBalance: BigDecimal?,

    open var endToEndReference: String?,
    open var customerReference: String?,
    open var mandateReference: String?,
    open var creditorIdentifier: String?,
    open var originatorsIdentificationCode: String?,
    open var compensationAmount: String?,
    open var originalAmount: String?,
    open var sepaUsage: String?,
    open var deviantOriginator: String?,
    open var deviantRecipient: String?,
    open var usageWithNoSpecialType: String?,
    open var primaNotaNumber: String?,
    open var textKeySupplement: String?,

    open var currencyType: String?,
    open var bookingKey: String,
    open var referenceForTheAccountOwner: String,
    open var referenceOfTheAccountServicingInstitution: String?,
    open var supplementaryDetails: String?,

    open var transactionReferenceNumber: String,
    open var relatedReferenceNumber: String?,
    var id: String = UUID.random().toString()
) {

    // for object deserializers
    internal constructor() : this(BankAccountEntity(), BigDecimal.Zero, "", "", Date(), null, null, null, null, Date(),
        -1, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "", "", null,
        null, "", null)

}
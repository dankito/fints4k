package net.dankito.banking.persistence.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import net.dankito.banking.ui.model.IAccountTransaction
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.UUID


@JsonIdentityInfo(property = "technicalId", generator = ObjectIdGenerators.PropertyGenerator::class) // to avoid stack overflow due to circular references
// had to define all properties as 'var' 'cause MapStruct cannot handle vals
open class AccountTransactionEntity(
    override var bankAccount: BankAccountEntity,
    override var amount: BigDecimal,
    override var currency: String,
    override var unparsedUsage: String,
    override var bookingDate: Date,
    override var otherPartyName: String?,
    override var otherPartyBankCode: String?,
    override var otherPartyAccountId: String?,
    override var bookingText: String?,
    override var valueDate: Date,
    override var statementNumber: Int,
    override var sequenceNumber: Int?,
    override var openingBalance: BigDecimal?,
    override var closingBalance: BigDecimal?,

    override var endToEndReference: String?,
    override var customerReference: String?,
    override var mandateReference: String?,
    override var creditorIdentifier: String?,
    override var originatorsIdentificationCode: String?,
    override var compensationAmount: String?,
    override var originalAmount: String?,
    override var sepaUsage: String?,
    override var deviantOriginator: String?,
    override var deviantRecipient: String?,
    override var usageWithNoSpecialType: String?,
    override var primaNotaNumber: String?,
    override var textKeySupplement: String?,

    override var currencyType: String?,
    override var bookingKey: String,
    override var referenceForTheAccountOwner: String,
    override var referenceOfTheAccountServicingInstitution: String?,
    override var supplementaryDetails: String?,

    override var transactionReferenceNumber: String,
    override var relatedReferenceNumber: String?,
    override var technicalId: String = UUID.random()
) : IAccountTransaction {

    // for object deserializers
    internal constructor() : this(BankAccountEntity(), BigDecimal.Zero, "", "", Date(), null, null, null, null, Date(),
        -1, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "", "", null,
        null, "", null)

    constructor(bankAccount: BankAccountEntity, otherPartyName: String?, unparsedUsage: String, amount: BigDecimal, valueDate: Date, bookingText: String?)
            : this(bankAccount, amount, "EUR", unparsedUsage, valueDate, otherPartyName, null, null, bookingText, valueDate, 0, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, "", "", null, null, "", null)


    override fun equals(other: Any?): Boolean {
        return doesEqual(other)
    }

    override fun hashCode(): Int {
        return calculateHashCode()
    }

}
package net.dankito.banking.ui.model

import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.DateFormatter


interface IAccountTransaction {

    companion object {
        val IdDateFormat = DateFormatter("yyyy.MM.dd")
    }


    val bankAccount: IBankAccount<*>
    val amount: BigDecimal
    val currency: String
    val unparsedUsage: String
    val bookingDate: Date
    val otherPartyName: String?
    val otherPartyBankCode: String?
    val otherPartyAccountId: String?
    val bookingText: String?
    val valueDate: Date
    val statementNumber: Int
    val sequenceNumber: Int?
    val openingBalance: BigDecimal?
    val closingBalance: BigDecimal?

    val endToEndReference: String?
    val customerReference: String?
    val mandateReference: String?
    val creditorIdentifier: String?
    val originatorsIdentificationCode: String?
    val compensationAmount: String?
    val originalAmount: String?
    val sepaUsage: String?
    val deviantOriginator: String?
    val deviantRecipient: String?
    val usageWithNoSpecialType: String?
    val primaNotaNumber: String?
    val textKeySupplement: String?

    val currencyType: String?
    val bookingKey: String
    val referenceForTheAccountOwner: String
    val referenceOfTheAccountServicingInstitution: String?
    val supplementaryDetails: String?

    val transactionReferenceNumber: String
    val relatedReferenceNumber: String?


    var technicalId: String


    val showOtherPartyName: Boolean
        get() = otherPartyName.isNullOrBlank() == false /* && type != "ENTGELTABSCHLUSS" && type != "AUSZAHLUNG" */ // TODO

    val canCreateMoneyTransferFrom: Boolean
        get() = otherPartyAccountId != null && bankAccount.supportsTransferringMoney

    val usage: String
        get() = sepaUsage ?: unparsedUsage


    fun buildTransactionIdentifier() : String {
        if (bankAccount != null) {
            return "${bankAccount.technicalId} ${IdDateFormat.format(bookingDate)} ${IdDateFormat.format(valueDate)} $amount $currency $unparsedUsage $otherPartyName $otherPartyBankCode $otherPartyAccountId"
        }
        else { // happens for derived classes during initialization. These have to set technicalId after initialization by themselves
            return "<uninitialized_bank_acccount> ${IdDateFormat.format(bookingDate)} ${IdDateFormat.format(valueDate)} $amount $currency $unparsedUsage $otherPartyName $otherPartyBankCode $otherPartyAccountId"
        }
    }
    
}
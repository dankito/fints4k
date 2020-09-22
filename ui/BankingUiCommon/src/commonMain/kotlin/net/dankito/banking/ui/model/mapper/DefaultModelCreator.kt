package net.dankito.banking.ui.model.mapper

import net.dankito.banking.ui.model.*
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date


open class DefaultModelCreator : IModelCreator {

    override fun createBank(bankCode: String, customerId: String, password: String, finTsServerAddress: String, bankName: String, bic: String,
                            customerName: String, userId: String, iconUrl: String?): TypedBankData {

        return BankData(bankCode, customerId, password, finTsServerAddress, bankName, bic, customerName, userId, iconUrl)
    }


    override fun createAccount(bank: TypedBankData, productName: String?, identifier: String): TypedBankAccount {
        return BankAccount(bank, productName, identifier)
    }

    override fun createTransaction(
        account: TypedBankAccount,
        amount: BigDecimal,
        currency: String,
        unparsedUsage: String,
        bookingDate: Date,
        otherPartyName: String?,
        otherPartyBankCode: String?,
        otherPartyAccountId: String?,
        bookingText: String?,
        valueDate: Date,
        statementNumber: Int,
        sequenceNumber: Int?,
        openingBalance: BigDecimal?,
        closingBalance: BigDecimal?,
        endToEndReference: String?,
        customerReference: String?,
        mandateReference: String?,
        creditorIdentifier: String?,
        originatorsIdentificationCode: String?,
        compensationAmount: String?,
        originalAmount: String?,
        sepaUsage: String?,
        deviantOriginator: String?,
        deviantRecipient: String?,
        usageWithNoSpecialType: String?,
        primaNotaNumber: String?,
        textKeySupplement: String?,
        currencyType: String?,
        bookingKey: String,
        referenceForTheAccountOwner: String,
        referenceOfTheAccountServicingInstitution: String?,
        supplementaryDetails: String?,
        transactionReferenceNumber: String,
        relatedReferenceNumber: String?
    ) : IAccountTransaction {

        return AccountTransaction(account, amount, currency, unparsedUsage, bookingDate,
            otherPartyName, otherPartyBankCode, otherPartyAccountId, bookingText, valueDate, statementNumber, sequenceNumber,
            openingBalance, closingBalance, endToEndReference, customerReference, mandateReference, creditorIdentifier,
            originatorsIdentificationCode, compensationAmount, originalAmount, sepaUsage, deviantOriginator, deviantRecipient,
            usageWithNoSpecialType, primaNotaNumber, textKeySupplement, currencyType, bookingKey, referenceForTheAccountOwner,
            referenceOfTheAccountServicingInstitution, supplementaryDetails, transactionReferenceNumber, relatedReferenceNumber)
    }

}
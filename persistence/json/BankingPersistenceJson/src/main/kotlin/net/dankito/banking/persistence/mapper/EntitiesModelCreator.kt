package net.dankito.banking.persistence.mapper

import net.dankito.banking.persistence.model.AccountTransactionEntity
import net.dankito.banking.persistence.model.BankAccountEntity
import net.dankito.banking.persistence.model.CustomerEntity
import net.dankito.banking.ui.model.IAccountTransaction
import net.dankito.banking.ui.model.TypedBankAccount
import net.dankito.banking.ui.model.TypedCustomer
import net.dankito.banking.ui.model.mapper.IModelCreator
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date


open class EntitiesModelCreator : IModelCreator {

    override fun createCustomer(bankCode: String, customerId: String, password: String, finTsServerAddress: String, bankName: String, bic: String,
                                customerName: String, userId: String, iconUrl: String?): TypedCustomer {

        return CustomerEntity(bankCode, customerId, password, finTsServerAddress, bankName, bic, customerName, userId, iconUrl) as TypedCustomer
    }


    override fun createBankAccount(customer: TypedCustomer, productName: String?, identifier: String): TypedBankAccount {
        return BankAccountEntity(customer as CustomerEntity, identifier, "", null, null, "", productName = productName) as TypedBankAccount
    }

    override fun createTransaction(
        bankAccount: TypedBankAccount,
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

        return AccountTransactionEntity(bankAccount as BankAccountEntity, amount, currency, unparsedUsage, bookingDate,
            otherPartyName, otherPartyBankCode, otherPartyAccountId, bookingText, valueDate, statementNumber, sequenceNumber,
            openingBalance, closingBalance, endToEndReference, customerReference, mandateReference, creditorIdentifier,
            originatorsIdentificationCode, compensationAmount, originalAmount, sepaUsage, deviantOriginator, deviantRecipient,
            usageWithNoSpecialType, primaNotaNumber, textKeySupplement, currencyType, bookingKey, referenceForTheAccountOwner,
            referenceOfTheAccountServicingInstitution, supplementaryDetails, transactionReferenceNumber, relatedReferenceNumber)
    }

}
package net.dankito.banking.ui.model.mapper

import net.dankito.banking.ui.model.*
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date


interface IModelCreator {
    
    fun createCustomer(bankCode: String, customerId: String, password: String, finTsServerAddress: String, bankName: String, bic: String,
                       customerName: String = "", userId: String = customerId, iconUrl: String? = null): TypedCustomer


    fun createBankAccount(customer: TypedCustomer, productName: String?, identifier: String) : TypedBankAccount


    fun createTransaction(
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
    ) : IAccountTransaction

}
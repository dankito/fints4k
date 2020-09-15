package net.dankito.banking.ui.model.mapper

import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.tan.*
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


    fun createTanProcedure(displayName: String, type: TanProcedureType, bankInternalProcedureCode: String,
                           maxTanInputLength: Int? = null, allowedTanFormat: AllowedTanFormat = AllowedTanFormat.Alphanumeric): TanProcedure {
        return TanProcedure(displayName, type, bankInternalProcedureCode, maxTanInputLength)
    }

    fun createTanMedium(displayName: String, status: TanMediumStatus): TanMedium {
        return TanMedium(displayName, status)
    }

    fun createTanGeneratorTanMedium(displayName: String, status: TanMediumStatus, cardNumber: String): TanGeneratorTanMedium {
        return TanGeneratorTanMedium(displayName, status, cardNumber)
    }

    fun createMobilePhoneTanMedium(displayName: String, status: TanMediumStatus, phoneNumber: String?): MobilePhoneTanMedium {
        return MobilePhoneTanMedium(displayName, status, phoneNumber)
    }

}
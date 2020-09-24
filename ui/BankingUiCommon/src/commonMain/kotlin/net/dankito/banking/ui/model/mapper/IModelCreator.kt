package net.dankito.banking.ui.model.mapper

import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.tan.*
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date


interface IModelCreator {
    
    fun createBank(bankCode: String, userName: String, password: String, finTsServerAddress: String, bankName: String, bic: String,
                   customerName: String = "", userId: String = userName, iconUrl: String? = null): TypedBankData


    fun createAccount(bank: TypedBankData, productName: String?, identifier: String) : TypedBankAccount


    fun createTransaction(
        account: TypedBankAccount,
        amount: BigDecimal,
        currency: String,
        unparsedReference: String,
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
        sepaReference: String?,
        deviantOriginator: String?,
        deviantRecipient: String?,
        referenceWithNoSpecialType: String?,
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


    fun createTanMethod(displayName: String, type: TanMethodType, bankInternalMethodCode: String,
                        maxTanInputLength: Int? = null, allowedTanFormat: AllowedTanFormat = AllowedTanFormat.Alphanumeric): TanMethod {
        return TanMethod(displayName, type, bankInternalMethodCode, maxTanInputLength)
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
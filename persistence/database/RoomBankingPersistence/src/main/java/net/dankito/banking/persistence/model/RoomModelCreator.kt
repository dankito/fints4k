package net.dankito.banking.persistence.model

import net.dankito.banking.ui.model.IAccountTransaction
import net.dankito.banking.ui.model.TypedBankAccount
import net.dankito.banking.ui.model.TypedBankData
import net.dankito.banking.ui.model.mapper.IModelCreator
import net.dankito.banking.ui.model.tan.AllowedTanFormat
import net.dankito.banking.ui.model.tan.TanMethodType
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date


open class RoomModelCreator : IModelCreator {

    override fun createBank(
        bankCode: String, userName: String, password: String, finTsServerAddress: String, bankName: String,
        bic: String, customerName: String, userId: String, iconData: ByteArray?
    ): TypedBankData {

        return Bank(bankCode, userName, password, finTsServerAddress, bankName, bic, customerName, userId, iconData)
    }

    override fun createAccount(bank: TypedBankData, productName: String?, identifier: String): TypedBankAccount {
        return BankAccount(bank, productName, identifier)
    }

    override fun createTransaction(
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
    ): IAccountTransaction {
        return AccountTransaction(account as BankAccount, amount, currency, unparsedReference, bookingDate, otherPartyName, otherPartyBankCode, otherPartyAccountId,
            bookingText, valueDate, statementNumber, sequenceNumber, openingBalance, closingBalance, endToEndReference, customerReference, mandateReference,
            creditorIdentifier, originatorsIdentificationCode, compensationAmount, originalAmount, sepaReference, deviantOriginator, deviantRecipient,
            referenceWithNoSpecialType, primaNotaNumber, textKeySupplement, currencyType, bookingKey, referenceForTheAccountOwner,
            referenceOfTheAccountServicingInstitution, supplementaryDetails, transactionReferenceNumber, relatedReferenceNumber)
    }


    override fun createTanMethod(displayName: String, type: TanMethodType, bankInternalMethodCode: String, maxTanInputLength: Int?, allowedTanFormat: AllowedTanFormat): net.dankito.banking.ui.model.tan.TanMethod {
        return TanMethod(displayName, type, bankInternalMethodCode, maxTanInputLength, allowedTanFormat)
    }

}
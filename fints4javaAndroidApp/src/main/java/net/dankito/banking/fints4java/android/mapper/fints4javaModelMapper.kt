package net.dankito.banking.fints4java.android.mapper

import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.model.responses.GetTransactionsResponse
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.fints.model.AccountData
import net.dankito.fints.model.BankData
import net.dankito.fints.model.CustomerData
import net.dankito.fints.response.client.FinTsClientResponse
import net.dankito.fints.response.segments.AccountType
import java.math.BigDecimal


open class fints4javaModelMapper {

    open fun mapAccount(customer: CustomerData, bank: BankData): Account {
        val mappedBank = mapBank(bank)

        val account = Account(mappedBank, customer.customerId, customer.pin, customer.name, customer.userId)

        account.bankAccounts = mapBankAccounts(account, customer.accounts)
        account.supportedTanProcedures = mapTanProcedures(customer.supportedTanProcedures)
        account.tanMedia = mapTanMediums(customer.tanMedia)

        return account
    }

    open fun mapBank(bank: BankData): Bank {
        return Bank(bank.bankCode, bank.finTs3ServerAddress, bank.bic, bank.name)
    }

    open fun mapBankAccounts(account: Account, accountData: List<AccountData>): List<BankAccount> {
        return accountData.map { mapBankAccount(account, it) }
    }

    open fun mapBankAccount(account: Account, accountData: AccountData): BankAccount {
        return BankAccount(account, accountData.accountIdentifier, accountData.accountHolderName, accountData.iban,
            accountData.subAccountAttribute, BigDecimal.ZERO, accountData.currency ?: "EUR",
            mapBankAccountType(accountData.accountType))
    }

    open fun mapBankAccountType(type: AccountType?): BankAccountType {
        return when (type) {

            else -> BankAccountType.Giro
        }
    }

    open fun mapTransactions(bankAccount: BankAccount, transactions: List<net.dankito.fints.model.AccountTransaction>): List<AccountTransaction> {
        return transactions.map { mapTransaction(bankAccount, it) }
    }

    open fun mapTransaction(bankAccount: BankAccount, transaction: net.dankito.fints.model.AccountTransaction): AccountTransaction {
        return AccountTransaction(
            transaction.amount,
            transaction.currency,
            transaction.usage,
            transaction.bookingDate,
            transaction.otherPartyName,
            transaction.otherPartyBankCode,
            transaction.otherPartyAccountId,
            transaction.bookingText,
            bankAccount
        )
    }

    open fun mapTanProcedures(tanProcedures: List<net.dankito.fints.model.TanProcedure>): List<TanProcedure> {
        return tanProcedures.map { mapTanProcedure(it) }
    }

    open fun mapTanProcedure(tanProcedure: net.dankito.fints.model.TanProcedure): TanProcedure {
        return TanProcedure(
            tanProcedure.displayName,
            mapTanProcedureType(tanProcedure.type),
            tanProcedure.securityFunction.code
        )
    }

    open fun mapTanProcedureType(type: net.dankito.fints.model.TanProcedureType): TanProcedureType {
        return when (type) {
            net.dankito.fints.model.TanProcedureType.EnterTan -> TanProcedureType.EnterTan
            net.dankito.fints.model.TanProcedureType.ChipTanManuell -> TanProcedureType.ChipTanManuell
            net.dankito.fints.model.TanProcedureType.ChipTanOptisch -> TanProcedureType.ChipTanOptisch
            net.dankito.fints.model.TanProcedureType.ChipTanQrCode -> TanProcedureType.ChipTanQrCode
            net.dankito.fints.model.TanProcedureType.PhotoTan -> TanProcedureType.PhotoTan
            net.dankito.fints.model.TanProcedureType.SmsTan -> TanProcedureType.SmsTan
            net.dankito.fints.model.TanProcedureType.PushTan -> TanProcedureType.PushTan
        }
    }


    open fun mapTanMediums(tanMediums: List<net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedium>): List<TanMedium> {
        return tanMediums.map { mapTanMedium(it) }
    }

    open fun mapTanMedium(tanMedium: net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedium): TanMedium {
        val status = if (tanMedium.status.name.contains("Aktiv")) TanMediumStatus.Used else TanMediumStatus.Available

        return TanMedium(getDisplayNameForTanMedium(tanMedium), status, tanMedium)
    }

    protected open fun getDisplayNameForTanMedium(tanMedium: net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedium): String {
        if (tanMedium is TanGeneratorTanMedium) {
            var cardNumber = tanMedium.cardNumber
            tanMedium.followUpCardNumber?.let {
                cardNumber += " (Folgenummer $it)" // TODO: translate
            }

            tanMedium.mediaName?.let { mediaName ->
                return "$mediaName $cardNumber"
            }

            return "Karte $cardNumber" // TODO: translate
        }

        return tanMedium.mediumClass.name
    }


    fun mapResponse(account: Account, response: net.dankito.fints.response.client.AddAccountResponse): AddAccountResponse {
        var bookedTransactions = mapOf<BankAccount, List<AccountTransaction>>()
        var balances = mapOf<BankAccount, BigDecimal>()

        account.bankAccounts.firstOrNull()?.let { bankAccount -> // TODO: set bank account also on net.dankito.fints.response.client.GetTransactionsResponse
            bookedTransactions = mapOf(bankAccount to mapTransactions(bankAccount, response.bookedTransactions))
            response.balance?.let { balances = mapOf(bankAccount to it) }
        }

        return AddAccountResponse(response.isSuccessful, mapErrorToShowToUser(response),
            account, response.supportsRetrievingTransactionsOfLast90DaysWithoutTan,
            bookedTransactions,
            mapOf(), // TODO: map unbooked transactions
            balances)
    }

    fun mapResponse(account: Account, response: net.dankito.fints.response.client.GetTransactionsResponse): GetTransactionsResponse {
        val bankAccount = account.bankAccounts.first() // TODO: set bank account also on net.dankito.fints.response.client.GetTransactionsResponse

        return GetTransactionsResponse(response.isSuccessful, mapErrorToShowToUser(response),
            mapOf(bankAccount to mapTransactions(bankAccount, response.bookedTransactions)),
            mapOf(), // TODO: map unbooked transactions
            response.balance?.let { mapOf(bankAccount to it) } ?: mapOf())
    }

    open fun mapErrorToShowToUser(response: FinTsClientResponse): String? {
        return response.exception?.localizedMessage ?: response.errorsToShowToUser.joinToString("\n")
    }


    open fun mapTanProcedureBack(tanProcedure: TanProcedure): net.dankito.fints.model.TanProcedure {
        return net.dankito.fints.model.TanProcedure(
            tanProcedure.displayName,
            Sicherheitsfunktion.values().first { it.code == tanProcedure.bankInternalProcedureCode },
            mapTanProcedureTypeBack(tanProcedure.type)
        )
    }

    open fun mapTanProcedureTypeBack(type: TanProcedureType): net.dankito.fints.model.TanProcedureType {
        return when (type) {
            TanProcedureType.EnterTan -> net.dankito.fints.model.TanProcedureType.EnterTan
            TanProcedureType.ChipTanManuell -> net.dankito.fints.model.TanProcedureType.ChipTanManuell
            TanProcedureType.ChipTanOptisch -> net.dankito.fints.model.TanProcedureType.ChipTanOptisch
            TanProcedureType.ChipTanQrCode -> net.dankito.fints.model.TanProcedureType.ChipTanQrCode
            TanProcedureType.PhotoTan -> net.dankito.fints.model.TanProcedureType.PhotoTan
            TanProcedureType.SmsTan -> net.dankito.fints.model.TanProcedureType.SmsTan
            TanProcedureType.PushTan -> net.dankito.fints.model.TanProcedureType.PushTan
        }
    }

}
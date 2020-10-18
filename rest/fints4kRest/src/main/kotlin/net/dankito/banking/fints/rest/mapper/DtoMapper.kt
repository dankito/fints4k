package net.dankito.banking.fints.rest.mapper

import net.dankito.banking.fints.model.*
import net.dankito.banking.fints.response.client.AddAccountResponse
import net.dankito.banking.fints.response.client.FinTsClientResponse
import net.dankito.banking.fints.response.client.GetTransactionsResponse
import net.dankito.banking.fints.rest.model.dto.response.*
import java.math.BigDecimal


open class DtoMapper {

    open fun map(response: AddAccountResponse): AddAccountResponseDto {
        return AddAccountResponseDto(
            response.successful,
            mapErrorMessage(response),
            map(response.bank),
            map(response.retrievedData)
        )
    }


    protected open fun map(bank: BankData): BankResponseDto {
        return BankResponseDto(
            bank.bankCode,
            bank.userName,
            bank.finTs3ServerAddress,
            bank.bic,
            bank.bankName,
            bank.userId,
            bank.customerName,
            mapTanMethods(bank.tanMethodsAvailableForUser),
            if (bank.isTanMethodSelected) map(bank.selectedTanMethod) else null,
            bank.tanMedia,
            bank.supportedHbciVersions.map { it.name.replace("Hbci_", "HBCI ").replace("FinTs_", "FinTS ").replace('_', '.') }
        )
    }


    open fun mapTransactions(accountsTransactions: List<GetTransactionsResponse>): GetAccountsTransactionsResponseDto {
        return GetAccountsTransactionsResponseDto(accountsTransactions.map { map(it) })
    }

    open fun map(accountTransactions: GetTransactionsResponse): GetAccountTransactionsResponseDto {
        val retrievedData = accountTransactions.retrievedData.first()
        val balance = mapNullable(retrievedData.balance)
        val bookedTransactions = map(retrievedData.bookedTransactions)

        return GetAccountTransactionsResponseDto(
            retrievedData.accountData.accountIdentifier,
            retrievedData.accountData.productName,
            accountTransactions.successful,
            mapErrorMessage(accountTransactions),
            balance,
            bookedTransactions,
            listOf()
        )
    }


    protected open fun map(accountData: List<RetrievedAccountData>): List<BankAccountResponseDto> {
        return accountData.map { map(it) }
    }

    protected open fun map(accountData: RetrievedAccountData): BankAccountResponseDto {
        return BankAccountResponseDto(
            accountData.accountData.accountIdentifier,
            accountData.accountData.subAccountAttribute,
            accountData.accountData.iban,
            accountData.accountData.userName,
            accountData.accountData.accountType,
            accountData.accountData.currency,
            accountData.accountData.accountHolderName,
            accountData.accountData.productName,
            accountData.accountData.supportsRetrievingBalance,
            accountData.accountData.supportsRetrievingAccountTransactions,
            accountData.accountData.supportsTransferringMoney,
            accountData.accountData.supportsRealTimeTransfer,
            accountData.successfullyRetrievedData,
            mapNullable(accountData.balance),
            accountData.retrievedTransactionsFrom,
            accountData.retrievedTransactionsTo,
            map(accountData.bookedTransactions),
            listOf()
        )
    }


    protected open fun map(transactions: Collection<AccountTransaction>): Collection<AccountTransactionResponseDto> {
        return transactions.map { map(it) }
    }

    protected open fun map(transaction: AccountTransaction): AccountTransactionResponseDto {
        return AccountTransactionResponseDto(
            map(transaction.amount),
            transaction.reference,
            transaction.otherPartyName,
            transaction.otherPartyBankCode,
            transaction.otherPartyAccountId,
            transaction.bookingText,
            transaction.valueDate
        )
    }


    protected open fun mapTanMethods(tanMethods: List<TanMethod>): List<TanMethodResponseDto> {
        return tanMethods.map { map(it) }
    }

    protected open fun map(tanMethod: TanMethod): TanMethodResponseDto {
        return TanMethodResponseDto(
            tanMethod.displayName,
            tanMethod.securityFunction.code,
            tanMethod.type,
            tanMethod.hhdVersion?.name?.replace("HHD_", "")?.replace('_', '.'),
            tanMethod.maxTanInputLength,
            tanMethod.allowedTanFormat
        )
    }


    protected open fun map(money: Money): BigDecimal {
        return money.bigDecimal
    }

    protected open fun mapNullable(money: Money?): BigDecimal? {
        return money?.let { map(it) }
    }


    protected open fun mapErrorMessage(response: FinTsClientResponse): String? {
        // TODO: evaluate fields like isJobAllowed or tanRequiredButWeWereToldToAbortIfSo and set error message accordingly
        return response.errorMessage
            ?: if (response.errorsToShowToUser.isNotEmpty()) response.errorsToShowToUser.joinToString("\n") else null
    }

}
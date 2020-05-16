package net.dankito.banking.persistence

import net.dankito.banking.persistence.entities.AccountEntity
import net.dankito.banking.persistence.entities.AccountTransactionEntity
import net.dankito.banking.persistence.entities.BankAccountEntity
import net.dankito.banking.persistence.entities.BankEntity
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.Bank
import net.dankito.banking.ui.model.BankAccount


open class UiModelToEntityMapper {

    open fun mapBank(bank: Bank): BankEntity {
        return BankEntity(
            bank.name,
            bank.bankCode,
            bank.bic,
            bank.finTsServerAddress,
            bank.iconUrl
        )
    }

    open fun mapBank(bank: BankEntity): Bank {
        return Bank(
            bank.name,
            bank.bankCode,
            bank.bic,
            bank.finTsServerAddress,
            bank.iconUrl
        )
    }


    open fun mapAccount(account: Account): AccountEntity {
        val mappedAccount = AccountEntity(
            mapBank(account.bank),
            account.customerId,
            account.password,
            account.name,
            account.userId,
            listOf()
        )

        mappedAccount.bankAccounts = mapBankAccounts(mappedAccount, account.bankAccounts)

        return mappedAccount
    }

    open fun mapAccount(account: AccountEntity): Account {
        val mappedAccount = Account(
            mapBank(account.bank),
            account.customerId,
            account.password,
            account.name,
            account.userId,
            listOf()
        )

        mappedAccount.bankAccounts = mapBankAccounts(mappedAccount, account.bankAccounts)

        return mappedAccount
    }


    open fun mapBankAccounts(account: AccountEntity, bankAccounts: List<BankAccount>): List<BankAccountEntity> {
        return bankAccounts.map { mapBankAccount(account, it) }
    }

    open fun mapBankAccount(account: AccountEntity, bankAccount: BankAccount): BankAccountEntity {
        val mappedBankAccount = BankAccountEntity(
            account,
            bankAccount.identifier,
            bankAccount.accountHolderName,
            bankAccount.iban,
            bankAccount.subAccountNumber,
            bankAccount.balance,
            bankAccount.currency,
            bankAccount.type,
            listOf()
        )

        mappedBankAccount.transactions = mapBankAccountTransactions(mappedBankAccount, bankAccount.bookedTransactions)

        return mappedBankAccount
    }

    open fun mapBankAccounts(account: Account, bankAccounts: List<BankAccountEntity>): List<BankAccount> {
        return bankAccounts.map { mapBankAccount(account, it) }
    }

    open fun mapBankAccount(account: Account, bankAccount: BankAccountEntity): BankAccount {
        val mappedBankAccount = BankAccount(
            account,
            bankAccount.identifier,
            bankAccount.name,
            bankAccount.iban,
            bankAccount.subAccountNumber,
            bankAccount.balance,
            bankAccount.currency,
            bankAccount.type
        )

        mappedBankAccount.addBookedTransactions(mapBankAccountTransactions(mappedBankAccount, bankAccount.transactions))

        return mappedBankAccount
    }


    open fun mapBankAccountTransactions(bankAccount: BankAccountEntity, transactions: List<AccountTransaction>): List<AccountTransactionEntity> {
        return transactions.map { mapBankAccountTransaction(bankAccount, it) }
    }

    open fun mapBankAccountTransaction(bankAccount: BankAccountEntity, transaction: AccountTransaction): AccountTransactionEntity {
        return AccountTransactionEntity(
            transaction.amount,
            transaction.bookingDate,
            transaction.usage,
            transaction.otherPartyName,
            transaction.otherPartyBankCode,
            transaction.otherPartyAccountId,
            transaction.bookingText,
            transaction.balance,
            transaction.currency,
            bankAccount
        )
    }

    open fun mapBankAccountTransactions(bankAccount: BankAccount, transactions: List<AccountTransactionEntity>): List<AccountTransaction> {
        return transactions.map { mapBankAccountTransaction(bankAccount, it) }
    }

    open fun mapBankAccountTransaction(bankAccount: BankAccount, transaction: AccountTransactionEntity): AccountTransaction {
        return AccountTransaction(
            transaction.amount,
            transaction.bookingDate,
            transaction.usage,
            transaction.otherPartyName,
            transaction.otherPartyBankCode,
            transaction.otherPartyAccountId,
            transaction.bookingText,
            transaction.balance,
            transaction.currency,
            bankAccount
        )
    }

}
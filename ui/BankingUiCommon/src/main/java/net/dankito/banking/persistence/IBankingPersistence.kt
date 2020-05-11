package net.dankito.banking.persistence

import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount


interface IBankingPersistence {

    fun saveOrUpdateAccount(account: Account, allAccounts: List<Account>)

    fun deleteAccount(account: Account, allAccounts: List<Account>)

    fun readPersistedAccounts(): List<Account>


    fun saveOrUpdateAccountTransactions(bankAccount: BankAccount, transactions: List<AccountTransaction>)

}
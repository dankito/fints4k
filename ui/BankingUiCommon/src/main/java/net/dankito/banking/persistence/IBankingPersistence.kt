package net.dankito.banking.persistence

import net.dankito.banking.ui.model.Customer
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount


interface IBankingPersistence {

    fun saveOrUpdateAccount(customer: Customer, allCustomers: List<Customer>)

    fun deleteAccount(customer: Customer, allCustomers: List<Customer>)

    fun readPersistedAccounts(): List<Customer>


    fun saveOrUpdateAccountTransactions(bankAccount: BankAccount, transactions: List<AccountTransaction>)

}
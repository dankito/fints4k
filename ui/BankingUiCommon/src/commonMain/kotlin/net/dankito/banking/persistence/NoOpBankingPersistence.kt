package net.dankito.banking.persistence

import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.Customer
import net.dankito.utils.multiplatform.File


open class NoOpBankingPersistence : IBankingPersistence {

    override fun saveOrUpdateAccount(customer: Customer, allCustomers: List<Customer>) {

    }

    override fun deleteAccount(customer: Customer, allCustomers: List<Customer>) {

    }

    override fun readPersistedAccounts(): List<Customer> {
        return listOf()
    }


    override fun saveOrUpdateAccountTransactions(bankAccount: BankAccount, transactions: List<AccountTransaction>) {

    }


    override fun saveUrlToFile(url: String, file: File) {

    }

}
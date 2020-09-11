package net.dankito.banking.persistence

import net.dankito.banking.ui.model.*
import net.dankito.utils.multiplatform.File


open class NoOpBankingPersistence : IBankingPersistence {

    override fun saveOrUpdateAccount(customer: TypedCustomer, allCustomers: List<TypedCustomer>) {

    }

    override fun deleteAccount(customer: TypedCustomer, allCustomers: List<TypedCustomer>) {

    }

    override fun readPersistedAccounts(): List<TypedCustomer> {
        return listOf()
    }


    override fun saveOrUpdateAccountTransactions(bankAccount: TypedBankAccount, transactions: List<IAccountTransaction>) {

    }


    override fun saveUrlToFile(url: String, file: File) {

    }

}
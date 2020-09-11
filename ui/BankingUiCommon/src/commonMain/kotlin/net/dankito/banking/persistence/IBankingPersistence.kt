package net.dankito.banking.persistence

import net.dankito.banking.ui.model.*
import net.dankito.utils.multiplatform.File


interface IBankingPersistence {

    fun saveOrUpdateAccount(customer: TypedCustomer, allCustomers: List<TypedCustomer>)

    fun deleteAccount(customer: TypedCustomer, allCustomers: List<TypedCustomer>)

    fun readPersistedAccounts(): List<TypedCustomer>


    fun saveOrUpdateAccountTransactions(bankAccount: TypedBankAccount, transactions: List<IAccountTransaction>)

    fun saveUrlToFile(url: String, file: File)

}
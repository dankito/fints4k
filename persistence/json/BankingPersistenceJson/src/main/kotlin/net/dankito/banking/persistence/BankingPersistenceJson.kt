package net.dankito.banking.persistence

import net.dankito.banking.ui.model.Customer
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.util.ISerializer
import net.dankito.banking.util.JacksonJsonSerializer
import java.io.File


open class BankingPersistenceJson(
    protected val jsonFile: File,
    protected val serializer: ISerializer = JacksonJsonSerializer()
) : IBankingPersistence {


    init {
        jsonFile.absoluteFile.parentFile.mkdirs()
    }


    override fun saveOrUpdateAccount(customer: Customer, allCustomers: List<Customer>) {
        serializer.serializeObject(allCustomers, jsonFile)
    }

    override fun deleteAccount(customer: Customer, allCustomers: List<Customer>) {
        serializer.serializeObject(allCustomers, jsonFile)
    }

    override fun readPersistedAccounts(): List<Customer> {
        return serializer.deserializeListOr(jsonFile, Customer::class.java, listOf())
    }


    override fun saveOrUpdateAccountTransactions(bankAccount: BankAccount, transactions: List<AccountTransaction>) {
        // done when called saveOrUpdateAccount()
    }

}
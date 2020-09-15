package net.dankito.banking.persistence

import net.dankito.banking.persistence.model.CustomerEntity
import net.dankito.banking.ui.model.*
import net.dankito.utils.multiplatform.File
import net.dankito.banking.util.ISerializer
import net.dankito.banking.util.persistence.doSaveUrlToFile


open class BankingPersistenceJson(
    protected val jsonFile: File,
    protected val serializer: ISerializer
) : IBankingPersistence {

    init {
        jsonFile.absoluteFile.parentFile.mkdirs()
    }


    override fun saveOrUpdateAccount(customer: TypedCustomer, allCustomers: List<TypedCustomer>) {
        saveAllCustomers(allCustomers)
    }

    override fun deleteAccount(customer: TypedCustomer, allCustomers: List<TypedCustomer>) {
        saveAllCustomers(allCustomers)
    }

    override fun readPersistedAccounts(): List<TypedCustomer> {
        return serializer.deserializeListOr(jsonFile, CustomerEntity::class).map { it as TypedCustomer }
    }


    override fun saveOrUpdateAccountTransactions(bankAccount: TypedBankAccount, transactions: List<IAccountTransaction>) {
        // done when called saveOrUpdateAccount()
        // TODO: or also call saveAllCustomers()?
    }


    protected open fun saveAllCustomers(allCustomers: List<TypedCustomer>) {
        serializer.serializeObject(allCustomers, jsonFile)
    }


    override fun saveUrlToFile(url: String, file: File) {
        doSaveUrlToFile(url, file)
    }

}
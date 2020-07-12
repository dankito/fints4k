package net.dankito.banking.persistence

import net.dankito.utils.multiplatform.File
import net.dankito.banking.ui.model.Customer
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.util.ISerializer
import java.io.FileOutputStream
import java.net.URL


open class BankingPersistenceJson(
    protected val jsonFile: File,
    protected val serializer: ISerializer
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
        return serializer.deserializeListOr(jsonFile, Customer::class, listOf())
    }


    override fun saveOrUpdateAccountTransactions(bankAccount: BankAccount, transactions: List<AccountTransaction>) {
        // done when called saveOrUpdateAccount()
    }


    override fun saveUrlToFile(url: String, file: File) {
        URL(url).openConnection().getInputStream().buffered().use { iconInputStream ->
            FileOutputStream(file).use { fileOutputStream ->
                iconInputStream.copyTo(fileOutputStream)
            }
        }
    }

}
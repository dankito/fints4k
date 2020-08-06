package net.dankito.banking.persistence

import net.dankito.banking.persistence.mapper.CustomerConverter
import net.dankito.banking.persistence.model.CustomerEntity
import net.dankito.utils.multiplatform.File
import net.dankito.banking.ui.model.Customer
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.util.ISerializer
import org.mapstruct.factory.Mappers
import java.io.FileOutputStream
import java.net.URL


open class BankingPersistenceJson(
    protected val jsonFile: File,
    protected val serializer: ISerializer
) : IBankingPersistence {

    protected val mapper = Mappers.getMapper(CustomerConverter::class.java)


    init {
        jsonFile.absoluteFile.parentFile.mkdirs()
    }


    override fun saveOrUpdateAccount(customer: Customer, allCustomers: List<Customer>) {
        saveAllCustomers(allCustomers)
    }

    override fun deleteAccount(customer: Customer, allCustomers: List<Customer>) {
        saveAllCustomers(allCustomers)
    }

    override fun readPersistedAccounts(): List<Customer> {
        val deserializedCustomers = serializer.deserializeListOr(jsonFile, CustomerEntity::class)

        return mapper.mapCustomerEntities(deserializedCustomers)
    }


    override fun saveOrUpdateAccountTransactions(bankAccount: BankAccount, transactions: List<AccountTransaction>) {
        // done when called saveOrUpdateAccount()
        // TODO: or also call saveAllCustomers()?
    }


    protected open fun saveAllCustomers(allCustomers: List<Customer>) {
        val mappedCustomers = mapper.mapCustomers(allCustomers)

        serializer.serializeObject(mappedCustomers, jsonFile)
    }


    override fun saveUrlToFile(url: String, file: File) {
        URL(url).openConnection().getInputStream().buffered().use { iconInputStream ->
            FileOutputStream(file).use { fileOutputStream ->
                iconInputStream.copyTo(fileOutputStream)
            }
        }
    }

}
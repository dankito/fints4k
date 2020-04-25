package net.dankito.banking.persistence

import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.serialization.JacksonJsonSerializer
import java.io.File


open class BankingPersistenceJson(
    protected val jsonFile: File,
    protected val serializer: ISerializer = JacksonJsonSerializer()
) : IBankingPersistence {


    init {
        jsonFile.absoluteFile.parentFile.mkdirs()
    }


    override fun saveOrUpdateAccount(account: Account, allAccounts: List<Account>) {
        serializer.serializeObject(allAccounts, jsonFile)
    }

    override fun deleteAccount(account: Account, allAccounts: List<Account>) {
        serializer.serializeObject(allAccounts, jsonFile)
    }

    override fun readPersistedAccounts(): List<Account> {
        return serializer.deserializeListOr(jsonFile, Account::class.java, listOf())
    }


    override fun saveOrUpdateAccountTransactions(bankAccount: BankAccount, transactions: List<AccountTransaction>) {
        // done when called saveOrUpdateAccount()
    }

}
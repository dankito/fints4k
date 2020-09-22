package net.dankito.banking.persistence

import net.dankito.banking.persistence.model.BankDataEntity
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


    override fun saveOrUpdateBank(bank: TypedBankData, allBanks: List<TypedBankData>) {
        saveAllBanks(allBanks)
    }

    override fun deleteBank(bank: TypedBankData, allBanks: List<TypedBankData>) {
        saveAllBanks(allBanks)
    }

    override fun readPersistedBanks(): List<TypedBankData> {
        return serializer.deserializeListOr(jsonFile, BankDataEntity::class).map { it as TypedBankData }
    }


    override fun saveOrUpdateAccountTransactions(account: TypedBankAccount, transactions: List<IAccountTransaction>) {
        // done when called saveOrUpdateAccount()
        // TODO: or also call saveAllBanks()?
    }


    protected open fun saveAllBanks(allBanks: List<TypedBankData>) {
        serializer.serializeObject(allBanks, jsonFile)
    }


    override fun saveUrlToFile(url: String, file: File) {
        doSaveUrlToFile(url, file)
    }

}
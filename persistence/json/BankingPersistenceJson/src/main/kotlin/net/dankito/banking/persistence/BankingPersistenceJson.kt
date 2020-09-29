package net.dankito.banking.persistence

import net.dankito.banking.persistence.model.BankDataEntity
import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.settings.AppSettings
import net.dankito.utils.multiplatform.File
import net.dankito.banking.util.ISerializer
import net.dankito.banking.util.persistence.doSaveUrlToFile


open class BankingPersistenceJson(
    protected val databaseFolder: File,
    protected val serializer: ISerializer
) : IBankingPersistence {

    companion object {
        const val BanksJsonFileName = "accounts.json"

        const val AppSettingsJsonFileName = "app_settings.json"
    }


    protected val banksJsonFile: File

    protected val appSettingsJsonFile: File


    init {
        databaseFolder.mkdirs()

        banksJsonFile = File(databaseFolder, BanksJsonFileName)
        appSettingsJsonFile = File(databaseFolder, AppSettingsJsonFileName)
    }


    override fun saveOrUpdateBank(bank: TypedBankData, allBanks: List<TypedBankData>) {
        saveAllBanks(allBanks)
    }

    override fun deleteBank(bank: TypedBankData, allBanks: List<TypedBankData>) {
        saveAllBanks(allBanks)
    }

    override fun readPersistedBanks(): List<TypedBankData> {
        return serializer.deserializeListOr(banksJsonFile, BankDataEntity::class).map { it as TypedBankData }
    }


    override fun saveOrUpdateAccountTransactions(account: TypedBankAccount, transactions: List<IAccountTransaction>) {
        // done when called saveOrUpdateAccount()
        // TODO: or also call saveAllBanks()?
    }


    protected open fun saveAllBanks(allBanks: List<TypedBankData>) {
        serializer.serializeObject(allBanks, banksJsonFile)
    }


    override fun saveOrUpdateAppSettings(appSettings: AppSettings) {
        serializer.serializeObject(appSettings, appSettingsJsonFile)
    }

    override fun readPersistedAppSettings(): AppSettings? {
        return serializer.deserializeObject(appSettingsJsonFile, AppSettings::class)
    }


    override fun saveUrlToFile(url: String, file: File) {
        doSaveUrlToFile(url, file)
    }

}
package net.dankito.banking.persistence

import net.dankito.banking.persistence.model.BankDataEntity
import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.settings.AppSettings
import net.dankito.utils.multiplatform.File
import net.dankito.banking.util.ISerializer
import net.dankito.banking.util.persistence.downloadIcon


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

    protected var readBanks: List<TypedBankData>? = null


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
        val banks = serializer.deserializeListOr(banksJsonFile, BankDataEntity::class).map { it as TypedBankData }

        this.readBanks = banks

        return banks
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


    override fun saveBankIcon(bank: TypedBankData, iconUrl: String, fileExtension: String?) {
        bank.iconData = downloadIcon(iconUrl)

        readBanks?.let {
            saveOrUpdateBank(bank, it)
        }
    }

}
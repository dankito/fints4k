package net.dankito.banking.persistence

import android.content.Context
import androidx.room.Room
import net.dankito.banking.persistence.dao.BaseDao
import net.dankito.banking.persistence.dao.saveOrUpdate
import net.dankito.banking.persistence.model.*
import net.dankito.banking.search.ITransactionPartySearcher
import net.dankito.banking.search.TransactionParty
import net.dankito.banking.ui.model.IAccountTransaction
import net.dankito.banking.ui.model.TypedBankAccount
import net.dankito.banking.ui.model.TypedBankData
import net.dankito.banking.ui.model.settings.AppSettings
import net.dankito.banking.ui.model.tan.MobilePhoneTanMedium
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium
import net.dankito.banking.util.persistence.doSaveUrlToFile
import net.dankito.utils.multiplatform.File
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory


open class RoomBankingPersistence(applicationContext: Context, password: String? = null) : IBankingPersistence, ITransactionPartySearcher {

    companion object {
        const val AppSettingsId = 1

        const val FlickerCodeTanMethodSettingsId = 1
        const val QrCodeTanMethodSettingsId = 2
        const val PhotoTanTanMethodSettingsId = 3
    }

    protected val db: BankingDatabase

    init {
        val passphrase = password?.let { SQLiteDatabase.getBytes(password.toCharArray()) } ?: ByteArray(0)
        val factory = SupportFactory(passphrase)

        db = Room.databaseBuilder(applicationContext, BankingDatabase::class.java, "banking-database")
            .openHelperFactory(factory)
            .build()
    }


    override fun saveOrUpdateBank(bank: TypedBankData, allBanks: List<TypedBankData>) {
        (bank as? Bank)?.let { bank ->
            bank.selectedTanMethodId = bank.selectedTanMethod?.technicalId

            db.bankDao().saveOrUpdate(bank)

            // TODO: in this way removed accounts won't be deleted from DB and therefore still be visible to user
            val accounts = bank.accounts.filterIsInstance<BankAccount>()
            accounts.forEach { it.bankId = bank.id }
            db.bankAccountDao().saveOrUpdate(accounts)

            // TODO: in this way removed TAN methods won't be deleted from DB and therefore still be visible to user
            val tanMethods = bank.supportedTanMethods.filterIsInstance<TanMethod>()
            tanMethods.forEach { tantanMethod ->
                if (tantanMethod.bankId == BaseDao.ObjectNotInsertedId) {
                    tantanMethod.bankId = bank.id
                    db.tanMethodDao().insert(tantanMethod)
                }
                else {
                    db.tanMethodDao().update(tantanMethod)
                }
            }

            // TODO: in this way removed TAN media won't be deleted from DB and therefore still be visible to user
            val tanMedia = bank.tanMedia.map { tanMedium ->
                bank.tanMediumEntities.firstOrNull { it.id == tanMedium.technicalId } ?: map(bank, tanMedium)
            }
            db.tanMediumDao().saveOrUpdate(tanMedia)
            bank.tanMediumEntities = tanMedia
        }
    }

    override fun deleteBank(bank: TypedBankData, allBanks: List<TypedBankData>) {
        (bank as? Bank)?.let { bank ->
            db.accountTransactionDao().delete(bank.accounts.flatMap { it.bookedTransactions }.filterIsInstance<AccountTransaction>())

            db.bankAccountDao().delete(bank.accounts.filterIsInstance<BankAccount>())

            db.tanMethodDao().delete(bank.supportedTanMethods.filterIsInstance<TanMethod>())
            db.tanMediumDao().delete(bank.tanMedia.filterIsInstance<TanMedium>())

            db.bankDao().delete(bank)
        }
    }

    override fun readPersistedBanks(): List<TypedBankData> {
        val banks = db.bankDao().getAll()

        val accounts = db.bankAccountDao().getAll()

        val transactions = db.accountTransactionDao().getAll()

        val tanMethods = db.tanMethodDao().getAll()

        val tanMedia = db.tanMediumDao().getAll()

        banks.forEach { bank ->
            bank.accounts = accounts.filter { it.bankId == bank.id }

            bank.accounts.filterIsInstance<BankAccount>().forEach { account ->
                account.bank = bank

                account.bookedTransactions = transactions.filter { it.accountId == account.id }

                account.bookedTransactions.filterIsInstance<AccountTransaction>().forEach { transaction ->
                    transaction.account = account
                }
            }

            bank.supportedTanMethods = tanMethods.filter { it.bankId == bank.id }
            bank.selectedTanMethod = bank.supportedTanMethods.firstOrNull { it.technicalId == bank.selectedTanMethodId }

            bank.tanMediumEntities = tanMedia.filter { it.bankId == bank.id }
            bank.tanMedia = bank.tanMediumEntities.map { map(it) }
        }

        return banks
    }

    override fun saveOrUpdateAccountTransactions(account: TypedBankAccount, transactions: List<IAccountTransaction>) {
        val accountId = (account as? BankAccount)?.id ?: account.technicalId.toLong()

        val mappedTransactions = transactions.filterIsInstance<AccountTransaction>()

        mappedTransactions.forEach { it.accountId = accountId }

        db.accountTransactionDao().saveOrUpdate(mappedTransactions)
    }


    protected open fun map(bank: Bank, tanMedium: net.dankito.banking.ui.model.tan.TanMedium): TanMedium {
        val type = when (tanMedium) {
            is TanGeneratorTanMedium -> TanMediumType.TanGeneratorTanMedium
            is MobilePhoneTanMedium -> TanMediumType.MobilePhoneTanMedium
            else -> TanMediumType.OtherTanMedium
        }

        return TanMedium(tanMedium.technicalId, bank.id, type, tanMedium.displayName, tanMedium.status,
            (tanMedium as? TanGeneratorTanMedium)?.cardNumber, (tanMedium as? MobilePhoneTanMedium)?.phoneNumber)
    }

    protected open fun map(tanMedium: TanMedium): net.dankito.banking.ui.model.tan.TanMedium {
        val displayName = tanMedium.displayName
        val status = tanMedium.status

        val mapped = when (tanMedium.type) {
            TanMediumType.TanGeneratorTanMedium -> TanGeneratorTanMedium(displayName, status, tanMedium.cardNumber ?: "")
            TanMediumType.MobilePhoneTanMedium -> MobilePhoneTanMedium(displayName, status, tanMedium.phoneNumber)
            else -> net.dankito.banking.ui.model.tan.TanMedium(displayName, status)
        }

        mapped.technicalId = tanMedium.id

        return mapped
    }


    override fun saveOrUpdateAppSettings(appSettings: AppSettings) {
        val mapped = net.dankito.banking.persistence.model.AppSettings(appSettings.updateAccountsAutomatically, appSettings.refreshAccountsAfterMinutes)
        db.appSettingsDao().saveOrUpdate(mapped)

        saveOrUpdateTanMethodSettings(appSettings.flickerCodeSettings, FlickerCodeTanMethodSettingsId)
        saveOrUpdateTanMethodSettings(appSettings.qrCodeSettings, QrCodeTanMethodSettingsId)
        saveOrUpdateTanMethodSettings(appSettings.photoTanSettings, PhotoTanTanMethodSettingsId)
    }

    protected open fun saveOrUpdateTanMethodSettings(settings: net.dankito.banking.ui.model.settings.TanMethodSettings?, id: Int) {
        settings?.let {
            val settingsEntity = TanMethodSettings(id, it.width, it.height, it.space, it.frequency)

            db.tanMethodSettingsDao().saveOrUpdate(settingsEntity)
        }
    }

    override fun readPersistedAppSettings(): AppSettings? {
        val tanMethodSettings = db.tanMethodSettingsDao().getAll()

        val settings = AppSettings()

        db.appSettingsDao().getAll().firstOrNull { it.id == AppSettingsId }?.let { persistedSettings ->
            settings.updateAccountsAutomatically = persistedSettings.updateAccountsAutomatically
            settings.refreshAccountsAfterMinutes = persistedSettings.refreshAccountsAfterMinutes
        }

        settings.flickerCodeSettings = findTanMethodSettings(FlickerCodeTanMethodSettingsId, tanMethodSettings)
        settings.qrCodeSettings = findTanMethodSettings(QrCodeTanMethodSettingsId, tanMethodSettings)
        settings.photoTanSettings = findTanMethodSettings(PhotoTanTanMethodSettingsId, tanMethodSettings)

        return settings
    }

    protected open fun findTanMethodSettings(id: Int, settings: List<TanMethodSettings>): TanMethodSettings? {
        return settings.firstOrNull { it.id == id }
    }


    override fun saveUrlToFile(url: String, file: File) {
        doSaveUrlToFile(url, file)
    }


    override fun findTransactionParty(query: String): List<TransactionParty> {
        return db.accountTransactionDao().findTransactionParty(query)
            .toSet() // don't display same transaction party multiple times
            .filterNot { it.bankCode.isNullOrBlank() || it.accountId.isNullOrBlank() }
            .map { TransactionParty(it.name, it.accountId, it.bankCode) }
    }

}
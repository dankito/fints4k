package net.dankito.banking.persistence

import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.settings.AppSettings
import net.dankito.utils.multiplatform.File


interface IBankingPersistence {

    fun saveOrUpdateBank(bank: TypedBankData, allBanks: List<TypedBankData>)

    fun deleteBank(bank: TypedBankData, allBanks: List<TypedBankData>)

    fun readPersistedBanks(): List<TypedBankData>


    fun saveOrUpdateAccountTransactions(account: TypedBankAccount, transactions: List<IAccountTransaction>)


    fun saveOrUpdateAppSettings(appSettings: AppSettings)

    fun readPersistedAppSettings(): AppSettings?


    fun saveBankIcon(bank: TypedBankData, iconUrl: String, fileExtension: String?)

}
package net.dankito.banking.persistence

import net.dankito.banking.ui.model.*
import net.dankito.utils.multiplatform.File


open class NoOpBankingPersistence : IBankingPersistence {

    override fun saveOrUpdateBank(bank: TypedBankData, allBanks: List<TypedBankData>) {

    }

    override fun deleteBank(bank: TypedBankData, allBanks: List<TypedBankData>) {

    }

    override fun readPersistedBanks(): List<TypedBankData> {
        return listOf()
    }


    override fun saveOrUpdateAccountTransactions(account: TypedBankAccount, transactions: List<IAccountTransaction>) {

    }


    override fun saveUrlToFile(url: String, file: File) {

    }

}
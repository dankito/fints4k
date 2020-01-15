package net.dankito.banking.persistence

import net.dankito.banking.ui.model.Account


interface IBankingPersistence {

    fun saveOrUpdateAccount(account: Account, allAccounts: List<Account>)

    fun readPersistedAccounts(): List<Account>

}
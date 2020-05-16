package net.dankito.banking.persistence

import net.dankito.banking.persistence.dao.IAccountDao
import net.dankito.banking.ui.model.Account


open class JpaBankingPersister(
    protected val accountDao: IAccountDao,
    protected val mapper: UiModelToEntityMapper = UiModelToEntityMapper()
) : IBankingPersistence {


    override fun saveOrUpdateAccount(account: Account, allAccounts: List<Account>) {
        accountDao.saveOrUpdate(mapper.mapAccount(account))
    }

    override fun readPersistedAccounts(): List<Account> {
        return listOf() // TODO
    }

}
package net.dankito.banking.persistence.dao

import net.dankito.banking.persistence.entities.AccountEntity
import net.dankito.jpa.entitymanager.IEntityManager


open class AccountDao(entityManager: IEntityManager)
    : IAccountDao, CouchbaseBasedDao<AccountEntity>(AccountEntity::class.java, entityManager)
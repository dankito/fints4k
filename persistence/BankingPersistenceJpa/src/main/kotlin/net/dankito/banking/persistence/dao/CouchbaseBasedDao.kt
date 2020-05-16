package net.dankito.banking.persistence.dao

import net.dankito.banking.persistence.entities.BaseEntity
import net.dankito.jpa.entitymanager.IEntityManager


abstract class CouchbaseBasedDao<T : BaseEntity>(private val entityClass: Class<T>,
                                                 protected val entityManager: IEntityManager) : IBaseDao<T> {

    override fun getAll(): List<T> {
        return entityManager.getAllEntitiesOfType(entityClass)
    }

    override fun saveOrUpdate(entity: T) {
        if (entity.isPersisted() == false) {
            entityManager.persistEntity(entity)
        }
        else {
            entityManager.updateEntity(entity)
        }
    }

    override fun saveOrUpdate(entities: List<T>) {
        val unpersistedEntities = entities.filter { it.isPersisted() == false }
        unpersistedEntities.forEach { unpersistedEntity ->
            entityManager.persistEntity(unpersistedEntity)
        }

        val persistedEntities = ArrayList(entities)
        persistedEntities.removeAll(unpersistedEntities)
        entityManager.updateEntities(persistedEntities)
    }

    override fun delete(entity: T) {
        entityManager.deleteEntity(entity)
    }

}
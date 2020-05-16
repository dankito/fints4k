package net.dankito.banking.persistence.dao

import net.dankito.banking.persistence.entities.BaseEntity


interface IBaseDao<T : BaseEntity> {

    fun getAll(): List<T>

    fun saveOrUpdate(entity: T)

    fun saveOrUpdate(entities: List<T>)

    fun delete(entity: T)

}
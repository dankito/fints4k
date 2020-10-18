package net.dankito.banking.persistence.dao

import androidx.room.*


interface BaseDao<T> {

    companion object {
        const val ObjectNotInsertedId = -1L

        const val IdNotSet = 0L
    }


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(obj: T): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(obj: List<T>): List<Long>


    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(obj: T)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(obj: List<T>)


    @Delete
    fun delete(obj: T)

    @Delete
    fun delete(obj: List<T>)

}
package net.dankito.banking.persistence.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.dankito.banking.persistence.room.entities.BankEntity


@Dao
interface BankDao {

    @Query("SELECT * FROM ${BankEntity.TableName}")
    fun getAll(): List<BankEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bank: BankEntity)

//    @Query("SELECT * FROM Users WHERE userid = :id")
//    fun getUserById(id: String): Flowable<User>

}
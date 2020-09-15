package net.dankito.banking.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import net.dankito.banking.persistence.model.BankAccount


@Dao
interface BankAccountDao : BaseDao<BankAccount> {

    @Query("SELECT * FROM BankAccount")
    fun getAll(): List<BankAccount>

}
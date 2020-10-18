package net.dankito.banking.persistence.dao

import androidx.room.*
import net.dankito.banking.persistence.model.Bank


@Dao
interface BankDao : BaseDao<Bank> {

    @Query("SELECT * FROM Bank")
    fun getAll(): List<Bank>

}
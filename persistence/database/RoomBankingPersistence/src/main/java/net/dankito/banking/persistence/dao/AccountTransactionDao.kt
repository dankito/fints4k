package net.dankito.banking.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import net.dankito.banking.persistence.model.AccountTransaction


@Dao
interface AccountTransactionDao : BaseDao<AccountTransaction> {

    @Query("SELECT * FROM AccountTransaction")
    fun getAll(): List<AccountTransaction>

}
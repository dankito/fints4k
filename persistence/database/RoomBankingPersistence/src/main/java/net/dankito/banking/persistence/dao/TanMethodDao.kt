package net.dankito.banking.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import net.dankito.banking.persistence.model.TanMethod


@Dao
interface TanMethodDao : BaseDao<TanMethod> {

    @Query("SELECT * FROM TanMethod")
    fun getAll(): List<TanMethod>

}
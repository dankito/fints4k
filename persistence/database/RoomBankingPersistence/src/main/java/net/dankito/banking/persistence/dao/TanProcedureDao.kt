package net.dankito.banking.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import net.dankito.banking.persistence.model.TanProcedure


@Dao
interface TanProcedureDao : BaseDao<TanProcedure> {

    @Query("SELECT * FROM TanProcedure")
    fun getAll(): List<TanProcedure>

}
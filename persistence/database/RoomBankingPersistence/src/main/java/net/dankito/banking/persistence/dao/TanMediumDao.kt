package net.dankito.banking.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import net.dankito.banking.persistence.model.TanMedium


@Dao
interface TanMediumDao : BaseDao<TanMedium> {

    @Query("SELECT * FROM TanMedium")
    fun getAll(): List<TanMedium>

}
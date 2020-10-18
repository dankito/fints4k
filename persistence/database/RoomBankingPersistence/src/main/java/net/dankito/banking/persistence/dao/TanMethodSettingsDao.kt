package net.dankito.banking.persistence.dao

import androidx.room.*
import net.dankito.banking.persistence.model.TanMethodSettings


@Dao
interface TanMethodSettingsDao : BaseDao<TanMethodSettings> {

    @Query("SELECT * FROM TanMethodSettings")
    fun getAll(): List<TanMethodSettings>

}
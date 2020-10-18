package net.dankito.banking.persistence.dao

import androidx.room.*
import net.dankito.banking.persistence.model.AppSettings


@Dao
interface AppSettingsDao : BaseDao<AppSettings> {

    @Query("SELECT * FROM AppSettings")
    fun getAll(): List<AppSettings>

}
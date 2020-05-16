package net.dankito.banking.persistence.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import net.dankito.banking.persistence.room.entities.BankEntity.Companion.TableName


@Entity(tableName = TableName)
data class BankEntity(

    @PrimaryKey
    @ColumnInfo(name = IdColumnName)
    val id: Long = 0,

    @ColumnInfo(name = "bankCode")
    val bankCode: String,

    var finTsServerAddress: String,

    var bic: String,

    var name: String,

    var iconUrl: String? = null

) {

    companion object {

        const val TableName = "banks"

        const val IdColumnName = "bank_id"

    }

}
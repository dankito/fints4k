package net.dankito.banking.persistence.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.dankito.banking.persistence.room.dao.BankDao
import net.dankito.banking.persistence.room.entities.BankEntity


@Database(entities = [BankEntity::class], version = 1)
abstract class BankingDatabase : RoomDatabase() {

    companion object {

        @Volatile private var INSTANCE: BankingDatabase? = null

        fun getInstance(context: Context): BankingDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                BankingDatabase::class.java, "banking.db")
                .build()
    }


    abstract fun bankDao(): BankDao

}
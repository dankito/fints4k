package net.dankito.banking.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.dankito.banking.persistence.dao.*
import net.dankito.banking.persistence.model.*


@Database(entities = [
    Bank::class, BankAccount::class, AccountTransaction::class,
    TanMethod::class, TanMedium::class,
    TanMethodSettings::class
], version = 1, exportSchema = false)
@TypeConverters(net.dankito.banking.persistence.TypeConverters::class)
abstract class BankingDatabase : RoomDatabase() {

    abstract fun bankDao(): BankDao

    abstract fun bankAccountDao(): BankAccountDao

    abstract fun accountTransactionDao(): AccountTransactionDao

    abstract fun tanMethodDao(): TanMethodDao

    abstract fun tanMediumDao(): TanMediumDao


    abstract fun tanMethodSettingsDao(): TanMethodSettingsDao

}
package net.dankito.banking.persistence

import androidx.room.TypeConverter
import net.dankito.banking.persistence.model.TanMediumType
import net.dankito.banking.ui.model.BankAccountType
import net.dankito.banking.ui.model.tan.AllowedTanFormat
import net.dankito.banking.ui.model.tan.TanMediumStatus
import net.dankito.banking.ui.model.tan.TanProcedureType
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date


open class TypeConverters {

    @TypeConverter
    fun fromMultiplatformBigDecimal(value: BigDecimal?): String? {
        return value?.toPlainString()
    }

    @TypeConverter
    fun toMultiplatformBigDecimal(value: String?): BigDecimal? {
        return value?.let { BigDecimal(value) }
    }


    @TypeConverter
    fun fromMultiplatformDate(value: Date?): Long? {
        return value?.millisSinceEpoch
    }

    @TypeConverter
    fun toMultiplatformDate(value: Long?): Date? {
        return value?.let { Date(value) }
    }


    @TypeConverter
    fun fromBankAccountType(value: BankAccountType): Int {
        return value.ordinal
    }

    @TypeConverter
    fun toBankAccountType(value: Int): BankAccountType {
        return BankAccountType.values().first { it.ordinal == value }
    }


    @TypeConverter
    fun fromTanProcedureType(value: TanProcedureType): Int {
        return value.ordinal
    }

    @TypeConverter
    fun toTanProcedureType(value: Int): TanProcedureType {
        return TanProcedureType.values().first { it.ordinal == value }
    }


    @TypeConverter
    fun fromAllowedTanFormat(value: AllowedTanFormat): Int {
        return value.ordinal
    }

    @TypeConverter
    fun toAllowedTanFormat(value: Int): AllowedTanFormat {
        return AllowedTanFormat.values().first { it.ordinal == value }
    }


    @TypeConverter
    fun fromTanMediumStatus(value: TanMediumStatus): Int {
        return value.ordinal
    }

    @TypeConverter
    fun toTanMediumStatus(value: Int): TanMediumStatus {
        return TanMediumStatus.values().first { it.ordinal == value }
    }


    @TypeConverter
    fun fromTanMediumTypes(value: TanMediumType): Int {
        return value.ordinal
    }

    @TypeConverter
    fun toTanMediumType(value: Int): TanMediumType {
        return TanMediumType.values().first { it.ordinal == value }
    }

}
package net.dankito.banking.fints.util

import com.soywiz.klock.Date
import com.soywiz.klock.DateTime
import com.soywiz.klock.Time
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Datum
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Uhrzeit


open class FinTsUtils {


    open fun formatDateToday(): String {
        return formatDate(DateTime.now().date)
    }

    open fun formatDate(date: Date): String {
        return Datum.format(date)
    }

    open fun formatDateTodayAsInt(): Int {
        return convertToInt(formatDateToday())
    }

    open fun formatDateAsInt(date: Date): Int {
        return convertToInt(formatDate(date))
    }


    open fun formatTimeNow(): String {
        return formatTime(DateTime.now().time)
    }

    open fun formatTime(time: Time): String {
        return Uhrzeit.format(time)
    }

    open fun formatTimeNowAsInt(): Int {
        return convertToInt(formatTimeNow())
    }

    open fun formatTimeAsInt(time: Time): Int {
        return convertToInt(formatTime(time))
    }


    protected fun convertToInt(string: String): Int {
        return string.toInt()
    }

}
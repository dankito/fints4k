package net.dankito.fints.util

import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Datum
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Uhrzeit
import java.util.*


open class FinTsUtils {


    open fun formatDateToday(): String {
        return formatDate(Date())
    }

    open fun formatDate(date: Date): String {
        return Datum.HbciDateFormat.format(date)
    }

    open fun formatDateTodayAsInt(): Int {
        return convertToInt(formatDateToday())
    }

    open fun formatDateAsInt(date: Date): Int {
        return convertToInt(formatDate(date))
    }


    open fun formatTimeNow(): String {
        return formatTime(Date())
    }

    open fun formatTime(time: Date): String {
        return Uhrzeit.HbciTimeFormat.format(time)
    }

    open fun formatTimeNowAsInt(): Int {
        return convertToInt(formatTimeNow())
    }

    open fun formatTimeAsInt(time: Date): Int {
        return convertToInt(formatTime(time))
    }


    protected fun convertToInt(string: String): Int {
        return string.toInt()
    }

}
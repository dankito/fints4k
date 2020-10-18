package net.dankito.utils.multiplatform

import java.text.DateFormat
import java.text.SimpleDateFormat


fun DateFormatStyle.convert(): Int {
    return when (this) {
        DateFormatStyle.Short -> DateFormat.SHORT
        DateFormatStyle.Medium -> DateFormat.MEDIUM
        DateFormatStyle.Long -> DateFormat.LONG
        DateFormatStyle.Full -> DateFormat.FULL
    }
}


actual class DateFormatter actual constructor(val pattern: String): SimpleDateFormat(pattern) {

    actual constructor(dateStyle: DateFormatStyle)
            : this((DateFormat.getDateInstance(dateStyle.convert()) as? SimpleDateFormat)?.toPattern() ?: "")

    actual constructor(dateStyle: DateFormatStyle, timeStyle: DateFormatStyle)
            : this((DateFormat.getDateTimeInstance(dateStyle.convert(), timeStyle.convert()) as? SimpleDateFormat)?.toPattern() ?: "")


    actual fun format(date: Date): String {
        return super.format(date)
    }

    actual override fun parse(dateString: String): Date? {
        super.parse(dateString)?.let { javaDate ->
            return Date(javaDate.time)
        }

        return null
    }

}
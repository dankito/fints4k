package net.dankito.banking.extensions

import com.soywiz.klock.jvm.toDate
import com.soywiz.klock.jvm.toDateTime
import java.text.SimpleDateFormat
import java.util.*


fun Date.toKlockDate(): com.soywiz.klock.Date {
    return this.toDateTime().date
}

fun com.soywiz.klock.Date.toJavaUtilDate(): Date {
    try {
        // there's a bug that some banks like Sparkasse return as date '190229' (= 29.02.2019), which simply doesn't exist
        return this.dateTimeDayStart.toDate()
    } catch (e: Exception) { // -> catch it and parse it manually, java.util.Date knows how to handle this
        return SimpleDateFormat("yyyy-MM-dd").parse(String.format("%04d-%02d-%02d", this.year, this.month1, this.day))
    }

}
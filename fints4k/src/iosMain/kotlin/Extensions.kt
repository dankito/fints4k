import com.soywiz.klock.Date
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.*


fun String.toNsData(): NSData {
    return this.encodeToByteArray().toNSData()
}

fun ByteArray.toNSData(): NSData = NSMutableData().apply {
    if (isEmpty()) return@apply
    this@toNSData.usePinned {
        appendBytes(it.addressOf(0), size.convert())
    }
}


fun NSDate?.toDate(): Date? {
    return this?.toDate()
}

fun NSDate.toDate(): Date {
    val calendar = NSCalendar.currentCalendar()

    return Date.invoke(
        calendar.component(NSCalendarUnitYear, this).toInt(),
        calendar.component(NSCalendarUnitMonth, this).toInt(),
        calendar.component(NSCalendarUnitDay, this).toInt()
    )
}
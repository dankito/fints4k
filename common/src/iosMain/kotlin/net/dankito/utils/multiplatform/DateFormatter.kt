package net.dankito.utils.multiplatform

import platform.Foundation.*


fun DateFormatStyle.convert(): ULong {
    return when (this) {
        DateFormatStyle.Short -> NSDateFormatterShortStyle
        DateFormatStyle.Medium -> NSDateFormatterMediumStyle
        DateFormatStyle.Long -> NSDateFormatterLongStyle
        DateFormatStyle.Full -> NSDateFormatterFullStyle
    }
}



actual class DateFormatter actual constructor(val pattern: String): NSDateFormatter() {

    actual constructor(dateStyle: DateFormatStyle) : this(NSDateFormatter().apply {
        this.dateStyle = dateStyle.convert()
    }.dateFormat) // TODO: does this work?

    actual constructor(dateStyle: DateFormatStyle, timeStyle: DateFormatStyle) : this(NSDateFormatter().apply {
        this.dateStyle = dateStyle.convert()
        this.timeStyle = timeStyle.convert()
    }.dateFormat) // TODO: does this work?


    init {
        this.dateFormat = pattern

        this.timeZone = NSTimeZone.localTimeZone // TODO: needed?
    }


    actual fun format(date: Date): String {
        return stringFromDate(date)
    }

}
package net.dankito.utils.multiplatform

import platform.Foundation.*


fun NSTimeInterval.toMillis(): Long {
    return this.toLong() * 1000
}


actual class Date(val date: NSDate) { // cannot subclass NSDate as it's a class cluster

    companion object {

        val DiffBetweenEpochTimeAndReferenceDate = (NSDate.timeIntervalSinceReferenceDate - NSTimeIntervalSince1970).toMillis()

        fun from(year: Int, month: Int, day: Int): NSDate {
            val dateComponents = NSDateComponents()

            dateComponents.year = year.toLong()
            dateComponents.month = month.toLong()
            dateComponents.day = day.toLong()

            return NSCalendar.currentCalendar.dateFromComponents(dateComponents) !!
        }

    }


    actual constructor(millisSinceEpoch: Long) : this(NSDate(timeIntervalSinceReferenceDate = ((millisSinceEpoch - DiffBetweenEpochTimeAndReferenceDate) / 1000).toDouble()))

    actual constructor() : this(NSDate())

    actual constructor(year: Int, month: Int, day: Int) : this(from(year, month, day))

    actual constructor(year: Int, month: Month, day: Int) : this(year, month.month, day)


    actual val millisSinceEpoch: Long
        get() = date.timeIntervalSince1970.toMillis()


    actual fun year(): Int {
        val components = NSCalendar.currentCalendar.components(NSCalendarUnitYear, date)
        return components.year.toInt()
    }

    actual fun month(): Month {
        return Month.fromInt(monthInt())
    }

    actual fun monthInt(): Int {
        val components = NSCalendar.currentCalendar.components(NSCalendarUnitMonth, date)
        return components.month.toInt()
    }

    actual fun day(): Int {
        val components = NSCalendar.currentCalendar.components(NSCalendarUnitDay, date)
        return components.day.toInt()
    }

}
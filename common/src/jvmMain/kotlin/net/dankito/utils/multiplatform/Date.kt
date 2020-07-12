package net.dankito.utils.multiplatform

import java.text.DateFormat


actual class Date actual constructor(millisSinceEpoch: Long) : java.util.Date(millisSinceEpoch) {

    actual constructor() : this(System.currentTimeMillis())

    actual constructor(year: Int, month: Int, day: Int) : this(java.util.Date(year - 1900, month - 1, day).time)

    actual constructor(year: Int, month: Month, day: Int) : this(year, month.month, day)


    actual val millisSinceEpoch: Long
        get() = time


    actual fun year(): Int {
        return formatDate(DateFormat.YEAR_FIELD)
    }

    actual fun month(): Month {
        return Month.fromInt(monthInt())
    }

    actual fun monthInt(): Int {
        return formatDate(DateFormat.MONTH_FIELD)
    }

    actual fun day(): Int {
        return formatDate(DateFormat.DAY_OF_YEAR_FIELD)
    }

    private fun formatDate(dateFormatStyle: Int): Int {
        val dateStringString = DateFormat.getDateInstance(dateFormatStyle).format(this)

        return dateStringString.toInt()
    }

}
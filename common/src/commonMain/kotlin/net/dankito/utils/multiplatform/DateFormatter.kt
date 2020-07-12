package net.dankito.utils.multiplatform


expect class DateFormatter constructor(pattern: String) {

    constructor(dateStyle: DateFormatStyle)

    constructor(dateStyle: DateFormatStyle, timeStyle: DateFormatStyle)


    fun format(date: Date): String

    fun parse(dateString: String): Date?

}
package net.dankito.utils.multiplatform


fun Date.format(format: DateFormatter): String {
    return format.format(this)
}

fun Date.format(pattern: String): String {
    return this.format(DateFormatter(pattern))
}


expect class Date(millisSinceEpoch: Long) {

    constructor()

    constructor(year: Int, month: Int, day: Int)

    actual constructor(year: Int, month: Month, day: Int)


    val millisSinceEpoch: Long

    fun year(): Int

    fun month(): Month

    fun monthInt(): Int

    fun day(): Int

}
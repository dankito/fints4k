package net.dankito.utils.multiplatform


fun Date.format(format: DateFormatter): String {
    return format.format(this)
}

fun Date.format(pattern: String): String {
    return this.format(DateFormatter(pattern))
}


expect class Date(millisSinceEpoch: Long) {

    constructor()


    val millisSinceEpoch: Long

}
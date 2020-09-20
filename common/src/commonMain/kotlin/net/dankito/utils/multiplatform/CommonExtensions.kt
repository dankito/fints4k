package net.dankito.utils.multiplatform


val Char.isLowerCase: Boolean
    get() = toLowerCase() == this

val Char.isUpperCase: Boolean
    get() = isLowerCase == false


fun Date.isBefore(other: Date): Boolean {
    return compareTo(other) < 0
}

fun Date.isBeforeOrEquals(other: Date): Boolean {
    return compareTo(other) <= 0
}


fun Throwable.getInnerExceptionMessage(maxDepth: Int = 3): String {
    return this.getInnerException(maxDepth).message ?: ""
}

fun Throwable.getInnerException(maxDepth: Int = 3): Throwable {
    var innerException = this
    var depth = 0

    while (innerException.cause is Throwable && depth < maxDepth) {
        innerException = innerException.cause as Throwable
        depth++
    }

    return innerException
}
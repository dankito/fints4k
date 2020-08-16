package net.dankito.utils.multiplatform



fun Throwable?.getInnerExceptionMessage(maxDepth: Int = 3): String? {
    return this?.getInnerException(maxDepth)?.message
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
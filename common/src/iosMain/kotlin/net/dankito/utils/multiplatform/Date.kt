package net.dankito.utils.multiplatform

import platform.Foundation.*


fun NSTimeInterval.toMillis(): Long {
    return this.toLong() * 1000
}


actual class Date(val date: NSDate) { // cannot subclass NSDate as it's a class cluster

    companion object {
        val DiffBetweenEpochTimeAndReferenceDate = (NSDate.timeIntervalSinceReferenceDate - NSTimeIntervalSince1970).toMillis()
    }


    actual constructor(millisSinceEpoch: Long) : this(NSDate(timeIntervalSinceReferenceDate = ((millisSinceEpoch - DiffBetweenEpochTimeAndReferenceDate) / 1000).toDouble()))

    actual constructor() : this(NSDate())


    actual val millisSinceEpoch: Long
        get() = date.timeIntervalSince1970.toMillis()

}
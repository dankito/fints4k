package net.dankito.utils.multiplatform

import platform.Foundation.*


fun NSTimeInterval.toMillis(): Long {
    return this.toLong() * 1000
}


actual class Date actual constructor(millisSinceEpoch: Long)
    : NSDate(timeIntervalSinceReferenceDate = ((millisSinceEpoch - DiffBetweenEpochTimeAndReferenceDate) / 1000).toDouble()) { // TODO: does this work?

    companion object {
        val DiffBetweenEpochTimeAndReferenceDate = (NSDate.timeIntervalSinceReferenceDate - NSTimeIntervalSince1970).toMillis()
    }


    actual constructor() : this(NSDate().timeIntervalSince1970.toMillis())


    actual val millisSinceEpoch: Long
        get() = timeIntervalSince1970.toMillis()

}
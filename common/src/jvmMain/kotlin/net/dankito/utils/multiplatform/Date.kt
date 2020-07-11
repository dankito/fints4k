package net.dankito.utils.multiplatform


actual class Date actual constructor(millisSinceEpoch: Long) : java.util.Date(millisSinceEpoch) {

    actual constructor() : this(System.currentTimeMillis())


    actual val millisSinceEpoch: Long
        get() = time

}
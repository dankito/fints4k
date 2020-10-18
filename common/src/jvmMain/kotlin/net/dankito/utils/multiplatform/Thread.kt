package net.dankito.utils.multiplatform


actual class Thread(private val thread: java.lang.Thread) : java.lang.Thread() {

    actual companion object {

        actual val current: Thread
            get() = Thread(currentThread())


        actual fun printCurrentThreadStackTrace() {
            Thread.current.printStackTrace()
        }

    }


    actual constructor() : this(java.lang.Thread())


    actual val threadName: String
        get() = thread.name


    actual fun printStackTrace() {
        println("Stack trace of $threadName")

        thread.stackTrace.forEachIndexed { index, stackTraceElement ->
            println("[$index] $stackTraceElement")
        }
    }

}
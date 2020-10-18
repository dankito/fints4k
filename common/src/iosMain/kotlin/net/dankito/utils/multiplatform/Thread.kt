package net.dankito.utils.multiplatform

import platform.Foundation.NSOperationQueue
import platform.Foundation.NSThread


actual class Thread(private val thread: NSThread) {

    actual companion object {

        actual val current: Thread
            get() = Thread(NSThread.currentThread)


        actual fun printCurrentThreadStackTrace() {
            Thread.current.printStackTrace()
        }

    }


    actual constructor() : this(NSThread())


    actual val threadName: String
        get() {
            thread.name?.let { name ->
                if (name.isNotBlank()) {
                    return name
                }
            }

            return thread.description
                ?: NSOperationQueue.currentQueue?.underlyingQueue?.description
                ?: "Could not retrieve thread's name"
        }


    actual fun printStackTrace() {
        println("Stack trace of $threadName")

        NSThread.callStackSymbols.forEach { callStackSymbol ->
            println(callStackSymbol)
        }
    }

}
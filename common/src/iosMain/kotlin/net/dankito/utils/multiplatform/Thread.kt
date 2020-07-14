package net.dankito.utils.multiplatform

import platform.Foundation.NSOperationQueue
import platform.Foundation.NSThread


actual class Thread(private val thread: NSThread) {

    actual companion object {

        actual val current: Thread
            get() = Thread(NSThread.currentThread)

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

}
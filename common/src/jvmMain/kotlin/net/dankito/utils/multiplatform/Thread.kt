package net.dankito.utils.multiplatform


actual class Thread(private val thread: java.lang.Thread) : java.lang.Thread() {

    actual companion object {

        actual val current: Thread
            get() = Thread(currentThread())

    }


    actual constructor() : this(java.lang.Thread())


    actual val threadName: String
        get() = thread.name

}
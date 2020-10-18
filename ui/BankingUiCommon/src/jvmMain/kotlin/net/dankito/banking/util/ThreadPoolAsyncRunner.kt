package net.dankito.banking.util

import net.dankito.utils.IThreadPool


open class ThreadPoolAsyncRunner(protected val threadPool: IThreadPool) : IAsyncRunner {

    override fun runAsync(runnable: () -> Unit) {
        threadPool.runAsync(runnable)
    }

}
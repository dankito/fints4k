package net.dankito.banking.fints.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


open class JavaThreadPool : IThreadPool {

    protected val threadPool: ExecutorService = Executors.newCachedThreadPool()


    override fun runAsync(runnable: () -> Unit) {
        threadPool.execute(runnable)
    }

    override fun shutDown() {
        if (threadPool.isShutdown == false) {
            threadPool.shutdownNow()
        }
    }

}
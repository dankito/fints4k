package net.dankito.banking.fints.util


interface IThreadPool {

    fun runAsync(runnable: () -> Unit)

    fun shutDown()

}
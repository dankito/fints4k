package net.dankito.banking.util


interface IAsyncRunner {

    fun runAsync(runnable: () -> Unit)

}
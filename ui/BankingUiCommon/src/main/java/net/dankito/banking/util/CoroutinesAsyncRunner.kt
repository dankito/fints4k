package net.dankito.banking.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


open class CoroutinesAsyncRunner : IAsyncRunner {

    override fun runAsync(runnable: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            runnable()
        }
    }

}
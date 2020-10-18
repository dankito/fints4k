package net.dankito.banking.util

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


open class CoroutinesAsyncRunner : IAsyncRunner { // TODO: remove (coroutines in common)

    override fun runAsync(runnable: () -> Unit) {
        GlobalScope.launch {
            runnable()
        }
    }

}
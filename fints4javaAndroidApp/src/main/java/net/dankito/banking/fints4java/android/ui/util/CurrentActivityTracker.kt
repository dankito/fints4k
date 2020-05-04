package net.dankito.banking.fints4java.android.ui.util

import net.dankito.banking.fints4java.android.ui.activities.BaseActivity
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.schedule


open class CurrentActivityTracker {

    var currentActivity: BaseActivity? = null
        set(value) {
            field = value // TODO: check field != value

            if (value != null) {
                callAndClearNextActivitySetListeners(value)
            }
        }


    protected val nextActivitySetListeners = CopyOnWriteArrayList<(BaseActivity) -> Unit>()

    open fun addNextActivitySetListener(listener: (BaseActivity) -> Unit) {
        synchronized(nextActivitySetListeners) {
            nextActivitySetListeners.add(listener)
        }
    }

    protected open fun callAndClearNextActivitySetListeners(activity: BaseActivity) {
        synchronized(nextActivitySetListeners) {
            val listenersCopy = ArrayList(nextActivitySetListeners)

            nextActivitySetListeners.clear()

            Timer().schedule(500) { // wait some time till activity is initialized
                listenersCopy.forEach { it(activity) }
            }
        }
    }

}
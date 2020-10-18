package net.dankito.banking.ui.android.util

import net.dankito.banking.ui.android.activities.BaseActivity
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.schedule


open class CurrentActivityTracker {

    protected val nextActivitySetListeners = CopyOnWriteArrayList<(BaseActivity) -> Unit>()


    var currentActivity: BaseActivity? = null
        set(value) {
            field = value // TODO: check field != value

            if (value != null) {
                callAndClearNextActivitySetListeners(value)
            }
        }


    open fun currentOrNextActivity(activity: (BaseActivity) -> Unit) {
        currentActivity?.let {
            activity(it)
        }
        ?: addNextActivitySetListener {
            activity(it)
        }
    }

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
                activity.runOnUiThread {
                    listenersCopy.forEach { it(activity) }
                }
            }
        }
    }

}
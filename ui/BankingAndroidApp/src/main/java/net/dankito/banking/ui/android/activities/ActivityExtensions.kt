package net.dankito.banking.ui.android.activities

import android.app.Activity
import android.content.Intent
import android.util.DisplayMetrics


fun <T : Activity> Activity.navigateToActivity(activityClass: Class<T>) {
    val intent = Intent(applicationContext, activityClass)

    startActivity(intent)
}

val Activity.screenWidth: Int
    get() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        return displayMetrics.widthPixels
    }
package net.dankito.banking.ui.android.extensions

import android.content.Context
import android.os.IBinder
import android.view.View
import android.view.inputmethod.InputMethodManager


fun Context.hideKeyboard(anyViewInHierarchy: View, flags: Int = 0) {
    hideKeyboard(anyViewInHierarchy.windowToken, flags)
}

fun Context.hideKeyboard(windowToken: IBinder, flags: Int = 0) {
    val keyboard = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    keyboard.hideSoftInputFromWindow(windowToken, flags)
}
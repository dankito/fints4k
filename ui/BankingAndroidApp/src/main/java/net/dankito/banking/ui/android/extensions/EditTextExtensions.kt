package net.dankito.banking.ui.android.extensions

import android.view.KeyEvent
import android.widget.EditText


val EditText.textString: String
    get() = this.text.toString()


fun EditText.addEnterPressedListener(enterPressed: () -> Boolean) {
    this.setOnKeyListener { _, keyCode, event ->
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
            return@setOnKeyListener enterPressed()
        }

        false
    }
}
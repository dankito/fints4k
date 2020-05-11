package net.dankito.banking.fints4java.android.ui.extensions

import android.view.KeyEvent
import android.widget.EditText


fun EditText.addEnterPressedListener(enterPressed: () -> Boolean) {
    this.setOnKeyListener { _, keyCode, _ ->
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            return@setOnKeyListener enterPressed()
        }

        false
    }
}
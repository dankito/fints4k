package net.dankito.banking.fints4java.android.ui.extensions

import android.app.Dialog
import android.view.KeyEvent
import com.otaliastudios.autocomplete.Autocomplete


/**
 * This will not work if Dialog is null!
 *
 * Making the Dialog parameter nullable is just for convienience to be able to call
 * Autocomplete
 *  .on<>(view)
 *  .build()
 *  .closePopupOnBackButtonPress(dialog)
 */
fun Autocomplete<*>.closePopupOnBackButtonPress(dialog: Dialog?) {
    dialog?.setOnKeyListener { _, keyCode, _ ->
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (this.isPopupShowing) { // close autocomplete popup on back button press
                this.dismissPopup()
                return@setOnKeyListener true
            }
        }

        false
    }
}
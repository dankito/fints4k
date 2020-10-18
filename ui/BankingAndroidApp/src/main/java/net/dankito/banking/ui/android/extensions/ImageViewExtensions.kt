package net.dankito.banking.ui.android.extensions

import android.view.View
import android.widget.ImageView
import net.dankito.banking.ui.model.IBankData


fun ImageView.setIcon(bank: IBankData<*, *>) {
    try {
        val iconData = bank.iconData

        if (iconData != null) {
            this.visibility = View.VISIBLE
            this.setImageFromBytes(iconData)
        }
        else {
            this.visibility = View.GONE
            this.setImageURI(null)
        }
    } catch (e: Exception) {
        this.visibility = View.GONE
    }
}

fun ImageView.setImageFromBytes(data: ByteArray) {
    this.setImageBitmap(data.toBitmap())
}
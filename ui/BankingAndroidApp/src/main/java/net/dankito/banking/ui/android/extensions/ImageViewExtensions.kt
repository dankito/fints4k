package net.dankito.banking.ui.android.extensions

import android.net.Uri
import android.view.View
import android.widget.ImageView
import net.dankito.banking.ui.model.IBankData


fun ImageView.setIcon(bank: IBankData<*, *>) {
    try {
        val iconUrl = bank.iconUrl
        this.visibility = if (iconUrl == null) View.GONE else View.VISIBLE
        this.setImageURI(Uri.parse(iconUrl))
    } catch (e: Exception) {
        this.visibility = View.GONE
    }
}
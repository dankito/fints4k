package net.dankito.banking.ui.android.views

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import androidx.annotation.StringRes
import kotlinx.android.synthetic.main.view_info_popup.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.extensions.hideKeyboard


open class InfoPopupWindow(open val activity: Activity, open val info: String) {

    constructor(activity: Activity, @StringRes infoStringResId: Int) : this(activity, activity.getString(infoStringResId))


    open fun show(atLocationOf: View, gravity: Int = Gravity.TOP) {
        activity.layoutInflater.inflate(R.layout.view_info_popup, null)?.let { contentView ->
            activity.hideKeyboard(atLocationOf)

            contentView.txtInfo.text = info

            val popupWindow = PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            popupWindow.isFocusable = true
            popupWindow.isOutsideTouchable = true

            contentView.findViewById<Button>(R.id.btnDismissPopup)?.setOnClickListener { popupWindow.dismiss() }

            popupWindow.showAtLocation(atLocationOf, gravity, 0, 0)

            popupWindow.showAsDropDown(atLocationOf)
        }
    }
}
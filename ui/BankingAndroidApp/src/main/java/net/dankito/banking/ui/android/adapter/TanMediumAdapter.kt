package net.dankito.banking.ui.android.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.model.tan.TanMedium
import net.dankito.utils.android.extensions.asActivity
import net.dankito.utils.android.ui.adapter.ListAdapter


open class TanMediumAdapter : ListAdapter<TanMedium>() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val tanMedium = getItem(position)

        val view = convertView ?: parent?.context?.asActivity()?.layoutInflater?.inflate(
                                    R.layout.list_item_tan_medium, parent, false)

        view?.findViewById<TextView>(R.id.txtTanMediumDisplayName)?.text = tanMedium.displayName

        return view
    }

}
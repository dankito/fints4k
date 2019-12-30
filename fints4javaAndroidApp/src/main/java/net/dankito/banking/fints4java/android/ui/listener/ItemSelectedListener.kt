package net.dankito.banking.fints4java.android.ui.listener

import android.view.View
import android.widget.AdapterView


open class ItemSelectedListener(val itemSelected: (position: Int) -> Unit) : AdapterView.OnItemSelectedListener {

    override fun onNothingSelected(parent: AdapterView<*>?) { }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        itemSelected(position)
    }

}
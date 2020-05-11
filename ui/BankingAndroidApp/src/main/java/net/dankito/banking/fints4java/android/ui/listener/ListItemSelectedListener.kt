package net.dankito.banking.fints4java.android.ui.listener

import android.view.View
import android.widget.AdapterView
import net.dankito.utils.android.ui.adapter.ListAdapter


open class ListItemSelectedListener<T>(val adapter: ListAdapter<T>, val itemSelected: (item: T) -> Unit) : AdapterView.OnItemSelectedListener {

    override fun onNothingSelected(parent: AdapterView<*>?) { }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        itemSelected(adapter.getItem(position))
    }

}
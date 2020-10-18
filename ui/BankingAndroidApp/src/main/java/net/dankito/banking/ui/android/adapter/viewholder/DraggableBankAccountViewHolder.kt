package net.dankito.banking.ui.android.adapter.viewholder

import android.view.View
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.DraggableBankAccountAdapterItem


open class DraggableBankAccountViewHolder(view: View) : FastAdapter.ViewHolder<DraggableBankAccountAdapterItem>(view) {

    protected var accountDisplayName: TextView = view.findViewById(R.id.txtBankDisplayName)


    override fun bindView(item: DraggableBankAccountAdapterItem, payloads: List<Any>) {
        accountDisplayName.text = item.account.displayName
    }

    override fun unbindView(item: DraggableBankAccountAdapterItem) {
        accountDisplayName.text = null
    }

}
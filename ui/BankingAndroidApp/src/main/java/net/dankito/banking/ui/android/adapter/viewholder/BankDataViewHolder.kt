package net.dankito.banking.ui.android.adapter.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.BankDataAdapterItem
import net.dankito.banking.ui.android.extensions.setIcon


open class BankDataViewHolder(view: View) : FastAdapter.ViewHolder<BankDataAdapterItem>(view) {

    protected var bankIcon: ImageView = view.findViewById(R.id.imgBankIcon)
    protected var bankDisplayName: TextView = view.findViewById(R.id.txtBankDisplayName)


    override fun bindView(item: BankDataAdapterItem, payloads: List<Any>) {
        bankIcon.setIcon(item.bank)
        bankDisplayName.text = item.bank.displayName
    }

    override fun unbindView(item: BankDataAdapterItem) {
        bankDisplayName.text = null
        bankIcon.setImageURI(null)
    }

}
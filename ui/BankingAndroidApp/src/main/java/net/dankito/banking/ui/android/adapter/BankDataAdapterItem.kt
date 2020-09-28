package net.dankito.banking.ui.android.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.drag.IDraggable
import com.mikepenz.fastadapter.items.AbstractItem
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.extensions.setIcon
import net.dankito.banking.ui.model.TypedBankData


open class BankDataAdapterItem(open val bank: TypedBankData) : AbstractItem<BankDataAdapterItem.ViewHolder>(), IDraggable {

    override var isDraggable = true

    override val type: Int
        get() = R.id.bank_data_item_id

    override val layoutRes: Int
        get() = R.layout.list_item_bank_data

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }


    class ViewHolder(view: View) : FastAdapter.ViewHolder<BankDataAdapterItem>(view) {

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

}
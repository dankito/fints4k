package net.dankito.banking.ui.android.adapter

import android.view.View
import com.mikepenz.fastadapter.drag.IDraggable
import com.mikepenz.fastadapter.items.AbstractItem
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.viewholder.BankDataViewHolder
import net.dankito.banking.ui.model.TypedBankData


open class BankDataAdapterItem(open val bank: TypedBankData) : AbstractItem<BankDataViewHolder>(), IDraggable {

    override var isDraggable = true

    override val type: Int
        get() = R.id.bank_data_item_id

    override val layoutRes: Int
        get() = R.layout.list_item_bank_data

    override fun getViewHolder(v: View): BankDataViewHolder {
        return BankDataViewHolder(v)
    }

}
package net.dankito.banking.ui.android.adapter

import android.view.View
import com.mikepenz.fastadapter.drag.IDraggable
import com.mikepenz.fastadapter.items.AbstractItem
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.viewholder.DraggableBankAccountViewHolder
import net.dankito.banking.ui.model.TypedBankAccount


open class DraggableBankAccountAdapterItem(open val account: TypedBankAccount) : AbstractItem<DraggableBankAccountViewHolder>(), IDraggable {

    override var isDraggable = true

    override val type: Int
        get() = R.id.draggable_bank_account_item_id

    override val layoutRes: Int
        get() = R.layout.list_item_draggable_bank_account

    override fun getViewHolder(v: View): DraggableBankAccountViewHolder {
        return DraggableBankAccountViewHolder(v)
    }

}
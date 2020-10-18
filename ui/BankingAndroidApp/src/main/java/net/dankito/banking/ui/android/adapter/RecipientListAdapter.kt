package net.dankito.banking.ui.android.adapter

import android.view.View
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.viewholder.RecipientViewHolder
import net.dankito.banking.search.TransactionParty
import net.dankito.utils.android.ui.adapter.ListRecyclerAdapter


open class RecipientListAdapter(protected val itemClicked: ((TransactionParty) -> Unit)? = null) : ListRecyclerAdapter<TransactionParty, RecipientViewHolder>() {

    override fun getListItemLayoutId() = R.layout.list_item_recipient

    override fun createViewHolder(itemView: View): RecipientViewHolder {
        return RecipientViewHolder(itemView)
    }

    override fun bindItemToView(viewHolder: RecipientViewHolder, item: TransactionParty) {
        viewHolder.txtvwRecipientName.text = item.name

        viewHolder.txtvwRecipientBankName.text = item.bankName
        viewHolder.txtvwRecipientBankName.visibility = if (item.bankName.isNullOrBlank()) View.GONE else View.VISIBLE

        viewHolder.txtvwRecipientAccountId.text = item.iban

        viewHolder.txtvwRecipientBankCode.text = item.bic

        viewHolder.itemView.setOnClickListener {
            itemClicked?.invoke(item)
        }
    }

}
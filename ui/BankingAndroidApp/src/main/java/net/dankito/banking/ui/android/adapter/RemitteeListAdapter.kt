package net.dankito.banking.ui.android.adapter

import android.view.View
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.viewholder.RemitteeViewHolder
import net.dankito.banking.search.Remittee
import net.dankito.utils.android.ui.adapter.ListRecyclerAdapter


open class RemitteeListAdapter(protected val itemClicked: ((Remittee) -> Unit)? = null) : ListRecyclerAdapter<Remittee, RemitteeViewHolder>() {

    override fun getListItemLayoutId() = R.layout.list_item_remittee

    override fun createViewHolder(itemView: View): RemitteeViewHolder {
        return RemitteeViewHolder(itemView)
    }

    override fun bindItemToView(viewHolder: RemitteeViewHolder, item: Remittee) {
        viewHolder.txtvwRemitteeName.text = item.name

        viewHolder.txtvwRemitteeBankName.text = item.bankName

        viewHolder.txtvwRemitteeBankCode.text = item.iban

        viewHolder.itemView.setOnClickListener {
            itemClicked?.invoke(item)
        }
    }

}
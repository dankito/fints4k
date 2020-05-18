package net.dankito.banking.ui.android.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_remittee.view.*


open class RemitteeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtvwRemitteeName: TextView = itemView.txtvwRemitteeName

    val txtvwRemitteeBankCode: TextView = itemView.txtvwRemitteeBankCode

}
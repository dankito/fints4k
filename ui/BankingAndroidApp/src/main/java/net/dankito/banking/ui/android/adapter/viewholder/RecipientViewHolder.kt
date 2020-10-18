package net.dankito.banking.ui.android.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_recipient.view.*


open class RecipientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtvwRecipientName: TextView = itemView.txtvwRecipientName

    val txtvwRecipientBankName: TextView = itemView.txtvwRecipientBankName

    val txtvwRecipientAccountId: TextView = itemView.txtvwRecipientAccountId

    val txtvwRecipientBankCode: TextView = itemView.txtvwRecipientBankCode

}
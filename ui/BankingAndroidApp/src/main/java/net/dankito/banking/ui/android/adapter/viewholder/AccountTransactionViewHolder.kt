package net.dankito.banking.ui.android.adapter.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_account_transaction.view.*


open class AccountTransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtvwDate: TextView = itemView.txtvwDate

    val txtvwTransactionLabel: TextView = itemView.txtvwTransactionLabel

    val txtvwReference: TextView = itemView.txtvwReference

    val txtvwAmount: TextView = itemView.txtvwAmount

    val imgvwBankIcon: ImageView = itemView.imgvwBankIcon

}
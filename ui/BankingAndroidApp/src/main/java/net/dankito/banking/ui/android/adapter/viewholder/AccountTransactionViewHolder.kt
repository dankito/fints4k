package net.dankito.banking.ui.android.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_account_transaction.view.*


open class AccountTransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtvwBookingDate: TextView = itemView.txtvwBookingDate

    val txtvwBookingText: TextView = itemView.txtvwBookingText

    val txtvwOtherPartyName: TextView = itemView.txtvwOtherPartyName

    val txtvwUsage1: TextView = itemView.txtvwUsage1

    val txtvwUsage2: TextView = itemView.txtvwUsage2

    val txtvwAmount: TextView = itemView.txtvwAmount

}
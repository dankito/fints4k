package net.dankito.banking.fints4java.android.ui.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_account_transaction.view.*


open class AccountTransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtvwBookingDate: TextView = itemView.txtvwBookingDate

    val txtvwBookingText: TextView = itemView.txtvwBookingText

    val txtvwOtherPartyName: TextView = itemView.txtvwOtherPartyName

    val txtvwUsage1: TextView = itemView.txtvwUsage1

    val txtvwUsage2: TextView = itemView.txtvwUsage2

    val txtvwAmount: TextView = itemView.txtvwAmount

}
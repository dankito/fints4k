package net.dankito.banking.ui.android.adapter.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_bank_info.view.*


open class BankInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val imgSupportsFints30: ImageView = itemView.imgSupportsFints30

    val txtvwBankName: TextView = itemView.txtvwBankName

    val txtvwBankCode: TextView = itemView.txtvwBankCode

    val txtvwBankAddress: TextView = itemView.txtvwBankAddress

}
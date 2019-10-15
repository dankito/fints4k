package net.dankito.banking.fints4java.android.ui.adapter

import android.view.View
import net.dankito.banking.fints4java.android.R
import net.dankito.banking.fints4java.android.ui.adapter.viewholder.AccountTransactionViewHolder
import net.dankito.fints.model.AccountTransaction
import net.dankito.utils.android.extensions.setTextColorToColorResource
import net.dankito.utils.android.ui.adapter.ListRecyclerAdapter
import java.math.BigDecimal
import java.text.DateFormat


open class AccountTransactionAdapter
    : ListRecyclerAdapter<AccountTransaction, AccountTransactionViewHolder>() {

    companion object {
        val BookingDateFormat = DateFormat.getDateInstance(DateFormat.SHORT)
    }


    override fun getListItemLayoutId() = R.layout.list_item_account_transaction

    override fun createViewHolder(itemView: View): AccountTransactionViewHolder {
        return AccountTransactionViewHolder(itemView)
    }

    override fun bindItemToView(viewHolder: AccountTransactionViewHolder, item: AccountTransaction) {
        viewHolder.txtvwBookingDate.text = BookingDateFormat.format(item.bookingDate)

        viewHolder.txtvwBookingText.text = item.bookingText ?: ""

        viewHolder.txtvwOtherPartyName.text = item.otherPartyName ?: ""

        viewHolder.txtvwUsage1.text = item.usage

        viewHolder.txtvwUsage2.visibility = View.GONE // TODO
        viewHolder.txtvwUsage2.text = "" // TODO

        viewHolder.txtvwAmount.text = item.amount.toString()
        viewHolder.txtvwAmount.setTextColorToColorResource(if (item.amount >= BigDecimal.ZERO) R.color.positiveAmount else R.color.negativeAmount)
    }

}
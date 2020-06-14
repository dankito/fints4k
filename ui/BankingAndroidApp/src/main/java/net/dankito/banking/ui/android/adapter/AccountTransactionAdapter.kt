package net.dankito.banking.ui.android.adapter

import android.net.Uri
import android.view.ContextMenu
import android.view.View
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.viewholder.AccountTransactionViewHolder
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.android.extensions.asActivity
import net.dankito.utils.android.extensions.setTextColorToColorResource
import net.dankito.utils.android.ui.adapter.ListRecyclerAdapter
import java.math.BigDecimal
import java.text.DateFormat


open class AccountTransactionAdapter(protected val presenter: BankingPresenter)
    : ListRecyclerAdapter<AccountTransaction, AccountTransactionViewHolder>() {

    companion object {
        val ValueDateFormat = DateFormat.getDateInstance(DateFormat.SHORT)
    }


    var selectedTransaction: AccountTransaction? = null


    override fun getListItemLayoutId() = R.layout.list_item_account_transaction

    override fun createViewHolder(itemView: View): AccountTransactionViewHolder {
        val viewHolder = AccountTransactionViewHolder(itemView)

        itemView.setOnCreateContextMenuListener { menu, view, menuInfo -> createContextMenu(menu, view, menuInfo, viewHolder) }

        return viewHolder
    }

    override fun bindItemToView(viewHolder: AccountTransactionViewHolder, item: AccountTransaction) {
        viewHolder.txtvwDate.text = ValueDateFormat.format(item.valueDate)

        viewHolder.txtvwBookingText.text = item.bookingText ?: ""

        viewHolder.txtvwOtherPartyName.visibility = if (item.showOtherPartyName) View.VISIBLE else View.GONE
        viewHolder.txtvwOtherPartyName.text = item.otherPartyName ?: ""

        viewHolder.txtvwUsage1.text = item.usage

        viewHolder.txtvwUsage2.visibility = View.GONE // TODO
        viewHolder.txtvwUsage2.text = "" // TODO

        viewHolder.txtvwAmount.text = presenter.formatAmount(item.amount)
        viewHolder.txtvwAmount.setTextColorToColorResource(if (item.amount >= BigDecimal.ZERO) R.color.positiveAmount else R.color.negativeAmount)

        val iconUrl = item.bankAccount.customer.bank.iconUrl
        if (iconUrl != null && presenter.areAllAccountSelected) {
            viewHolder.imgvwBankIcon.visibility = View.VISIBLE
            viewHolder.imgvwBankIcon.setImageURI(Uri.parse(iconUrl))
        }
        else {
            viewHolder.imgvwBankIcon.visibility = View.GONE
        }
    }


    protected open fun createContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenu.ContextMenuInfo?,
                                         viewHolder: AccountTransactionViewHolder) {

        view.context.asActivity()?.menuInflater?.inflate(R.menu.context_menu_account_transactions, menu)

        selectedTransaction = getItem(viewHolder.adapterPosition)

        menu.findItem(R.id.mnitmShowTransferMoneyDialog)?.let { mnitmShowTransferMoneyDialog ->
            mnitmShowTransferMoneyDialog.isVisible = selectedTransaction?.bankAccount?.supportsTransferringMoney ?: false

            val remitteeName = selectedTransaction?.otherPartyName ?: ""

            mnitmShowTransferMoneyDialog.title = view.context.getString(R.string.fragment_home_transfer_money_to, remitteeName)
        }
    }

}
package net.dankito.banking.ui.android.adapter

import android.net.Uri
import android.view.ContextMenu
import android.view.View
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.viewholder.AccountTransactionViewHolder
import net.dankito.banking.ui.android.extensions.showAmount
import net.dankito.banking.ui.model.IAccountTransaction
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.android.extensions.asActivity
import net.dankito.utils.android.ui.adapter.ListRecyclerAdapter
import java.text.DateFormat


open class AccountTransactionAdapter(protected val presenter: BankingPresenter)
    : ListRecyclerAdapter<IAccountTransaction, AccountTransactionViewHolder>() {

    companion object {
        val ValueDateFormat = DateFormat.getDateInstance(DateFormat.SHORT)
    }


    var selectedTransaction: IAccountTransaction? = null


    override fun getListItemLayoutId() = R.layout.list_item_account_transaction

    override fun createViewHolder(itemView: View): AccountTransactionViewHolder {
        val viewHolder = AccountTransactionViewHolder(itemView)

        itemView.setOnCreateContextMenuListener { menu, view, menuInfo -> createContextMenu(menu, view, menuInfo, viewHolder) }

        return viewHolder
    }

    override fun bindItemToView(viewHolder: AccountTransactionViewHolder, item: IAccountTransaction) {
        viewHolder.txtvwDate.text = ValueDateFormat.format(item.valueDate)

        val label = if (item.showOtherPartyName) item.otherPartyName else item.bookingText
        viewHolder.txtvwTransactionLabel.text = label ?: item.bookingText ?: ""

        viewHolder.txtvwReference.text = item.reference

        viewHolder.txtvwAmount.showAmount(presenter, item.amount, item.currency)

        val iconUrl = item.account.bank.iconUrl
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

        val canCreateMoneyTransferFrom = selectedTransaction?.canCreateMoneyTransferFrom ?: false

        menu.findItem(R.id.mnitmNewTransferWithSameData)?.isVisible = canCreateMoneyTransferFrom

        menu.findItem(R.id.mnitmNewTransferToSameTransactionParty)?.let { mnitmShowTransferMoneyDialog ->
            mnitmShowTransferMoneyDialog.isVisible = canCreateMoneyTransferFrom

            val recipientName = selectedTransaction?.otherPartyName ?: ""

            mnitmShowTransferMoneyDialog.title = view.context.getString(R.string.fragment_home_transfer_money_to, recipientName)
        }
    }

}
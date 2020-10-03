package net.dankito.banking.ui.android.adapter

import android.view.ContextMenu
import android.view.View
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.viewholder.AccountTransactionViewHolder
import net.dankito.banking.ui.android.extensions.setIcon
import net.dankito.banking.ui.android.extensions.showAmount
import net.dankito.banking.ui.model.IAccountTransaction
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.android.extensions.asActivity
import net.dankito.utils.android.ui.adapter.ListRecyclerAdapter


open class AccountTransactionAdapter(protected val presenter: BankingPresenter)
    : ListRecyclerAdapter<IAccountTransaction, AccountTransactionViewHolder>() {


    var selectedTransaction: IAccountTransaction? = null


    override fun getListItemLayoutId() = R.layout.list_item_account_transaction

    override fun createViewHolder(itemView: View): AccountTransactionViewHolder {
        val viewHolder = AccountTransactionViewHolder(itemView)

        itemView.setOnCreateContextMenuListener { menu, view, menuInfo -> createContextMenu(menu, view, menuInfo, viewHolder) }

        return viewHolder
    }

    override fun bindItemToView(viewHolder: AccountTransactionViewHolder, item: IAccountTransaction) {
        viewHolder.txtvwDate.text = presenter.formatToShortDate(item.valueDate)

        val label = if (item.showOtherPartyName) item.otherPartyName else item.bookingText
        viewHolder.txtvwTransactionLabel.text = label ?: item.bookingText ?: ""

        viewHolder.txtvwReference.text = item.reference

        viewHolder.txtvwAmount.showAmount(presenter, item.amount, item.currency)

        if (presenter.areAllAccountSelected) {
            viewHolder.imgvwBankIcon.setIcon(item.account.bank)
            // TODO: if bank icon isn't set: Show default icon? show at least an empty space to that amount and date don't shift up causing an inconsistent view?
            viewHolder.imgvwBankIcon.visibility = View.VISIBLE
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
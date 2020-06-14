package net.dankito.banking.ui.javafx.model

import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import net.dankito.banking.ui.model.Customer
import tornadofx.FX.Companion.messages
import tornadofx.get
import tornadofx.runLater


open class AccountsRootTreeItem(customers: ObservableList<Customer>) : AccountsTreeItemBase(messages["accounts.view.all.accounts"]) {

    init {
        setAccounts(customers)

        customers.addListener(ListChangeListener {
            runLater { setAccounts(customers) }
        })
    }

    protected open fun setAccounts(customers: List<Customer>) {
        isExpanded = customers.isNotEmpty()

        children.setAll(customers.map { AccountsAccountTreeItem(it) })
    }

}
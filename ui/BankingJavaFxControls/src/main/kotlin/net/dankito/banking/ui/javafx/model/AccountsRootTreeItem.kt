package net.dankito.banking.ui.javafx.model

import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import net.dankito.banking.ui.model.Account
import tornadofx.FX.Companion.messages
import tornadofx.get
import tornadofx.runLater


open class AccountsRootTreeItem(accounts: ObservableList<Account>) : AccountsTreeItemBase(messages["accounts.view.all.accounts"]) {

    init {
        setAccounts(accounts)

        accounts.addListener(ListChangeListener {
            runLater { setAccounts(accounts) }
        })
    }

    protected open fun setAccounts(accounts: List<Account>) {
        isExpanded = accounts.isNotEmpty()

        children.setAll(accounts.map { AccountsAccountTreeItem(it) })
    }

}
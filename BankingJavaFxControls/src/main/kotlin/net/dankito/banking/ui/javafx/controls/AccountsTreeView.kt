package net.dankito.banking.ui.javafx.controls

import javafx.collections.ObservableList
import javafx.scene.control.TreeView
import net.dankito.banking.ui.javafx.model.AccountsRootTreeItem
import net.dankito.banking.ui.model.Account


open class AccountsTreeView(accounts: ObservableList<Account>) : TreeView<String>(AccountsRootTreeItem(accounts))
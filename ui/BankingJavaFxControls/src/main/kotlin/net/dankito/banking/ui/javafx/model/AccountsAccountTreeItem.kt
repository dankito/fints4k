package net.dankito.banking.ui.javafx.model

import javafx.scene.Node
import net.dankito.banking.ui.javafx.extensions.createBankIconImageView
import net.dankito.banking.ui.model.TypedBankData


open class AccountsAccountTreeItem(val bank: TypedBankData) : AccountsTreeItemBase(bank.displayName) {

    companion object {
        private const val IconSize = 16.0
    }


    init {
        isExpanded = true

        graphic = createIconImageView()

        bank.accounts.forEach { account ->
            children.add(AccountsBankAccountTreeItem(account))
        }
    }

    protected open fun createIconImageView(): Node? {
        return bank.createBankIconImageView(IconSize)
    }

}
package net.dankito.banking.ui.javafx.model

import net.dankito.banking.ui.model.Account


open class AccountsAccountTreeItem(val account: Account) : AccountsTreeItemBase(account.displayName) {

    init {
        isExpanded = true

        account.bankAccounts.forEach { bankAccount ->
            children.add(AccountsBankAccountTreeItem(bankAccount))
        }
    }

}
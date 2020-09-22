package net.dankito.banking.ui.javafx.model

import net.dankito.banking.ui.model.TypedBankAccount


open class AccountsBankAccountTreeItem(val account: TypedBankAccount) : AccountsTreeItemBase(account.displayName)
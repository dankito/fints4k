package net.dankito.banking.ui.javafx.model

import net.dankito.banking.ui.model.BankAccount


open class AccountsBankAccountTreeItem(bankAccount: BankAccount) : AccountsTreeItemBase(bankAccount.displayName)
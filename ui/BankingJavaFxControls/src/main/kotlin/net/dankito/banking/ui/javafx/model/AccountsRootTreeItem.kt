package net.dankito.banking.ui.javafx.model

import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import net.dankito.banking.ui.model.TypedBankData
import tornadofx.FX.Companion.messages
import tornadofx.get
import tornadofx.runLater


open class AccountsRootTreeItem(banks: ObservableList<TypedBankData>) : AccountsTreeItemBase(messages["accounts.view.all.accounts"]) {

    init {
        setBanks(banks)

        banks.addListener(ListChangeListener {
            runLater { setBanks(banks) }
        })
    }

    protected open fun setBanks(banks: List<TypedBankData>) {
        isExpanded = banks.isNotEmpty()

        children.setAll(banks.map { AccountsAccountTreeItem(it) })
    }

}
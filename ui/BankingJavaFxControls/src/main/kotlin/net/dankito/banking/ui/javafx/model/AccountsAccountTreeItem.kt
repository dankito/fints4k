package net.dankito.banking.ui.javafx.model

import javafx.scene.Node
import javafx.scene.image.ImageView
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
        bank.iconUrl?.let {
            val iconImageView = ImageView(it)

            iconImageView.fitHeight = IconSize
            iconImageView.fitWidth = IconSize
            iconImageView.isPreserveRatio = true

            return iconImageView
        }

        // TODO: otherwise set default icon
        return null
    }

}
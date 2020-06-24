package net.dankito.banking.ui.javafx.model

import javafx.scene.Node
import javafx.scene.image.ImageView
import net.dankito.banking.ui.model.Customer


open class AccountsAccountTreeItem(val customer: Customer) : AccountsTreeItemBase(customer.displayName) {

    companion object {
        private const val IconSize = 16.0
    }


    init {
        isExpanded = true

        graphic = createIconImageView()

        customer.accounts.forEach { bankAccount ->
            children.add(AccountsBankAccountTreeItem(bankAccount))
        }
    }

    protected open fun createIconImageView(): Node? {
        customer.iconUrl?.let {
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
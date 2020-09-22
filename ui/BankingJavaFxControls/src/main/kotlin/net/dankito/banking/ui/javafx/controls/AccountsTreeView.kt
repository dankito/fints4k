package net.dankito.banking.ui.javafx.controls

import javafx.collections.ObservableList
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.ContextMenu
import javafx.scene.control.TreeView
import javafx.scene.input.ContextMenuEvent
import javafx.scene.input.KeyCode
import net.dankito.banking.ui.javafx.dialogs.JavaFxDialogService
import net.dankito.banking.ui.javafx.model.AccountsAccountTreeItem
import net.dankito.banking.ui.javafx.model.AccountsRootTreeItem
import net.dankito.banking.ui.model.TypedBankData
import net.dankito.banking.ui.presenter.BankingPresenter
import tornadofx.*
import tornadofx.FX.Companion.messages


open class AccountsTreeView(banks: ObservableList<TypedBankData>, protected val presenter: BankingPresenter)
    : TreeView<String>(AccountsRootTreeItem(banks)) {

    protected var currentMenu: ContextMenu? = null


    init {
        setupUi()
    }


    protected open fun setupUi() {
        setOnContextMenuRequested { event -> showContextMenu(event) }

        setOnKeyReleased { event ->
            if (event.code == KeyCode.DELETE) {
                (selectionModel.selectedItem as? AccountsAccountTreeItem)?.let {
                    askIfAccountShouldBeDeleted(it)
                }
            }
        }
    }


    protected open fun showContextMenu(event: ContextMenuEvent) {
        currentMenu?.hide()

        (selectionModel.selectedItem as? AccountsAccountTreeItem)?.let {
            currentMenu = createContextMenuForItems(it)
            currentMenu?.show(this, event.screenX, event.screenY)
        }
    }

    protected open fun createContextMenuForItems(selectedItem: AccountsAccountTreeItem): ContextMenu {
        val contextMenu = ContextMenu()

        contextMenu.apply {
            item(messages["accounts.view.context.menu.delete.account"]) {
                action {
                    askIfAccountShouldBeDeleted(selectedItem)
                }
            }
        }

        return contextMenu
    }

    protected open fun askIfAccountShouldBeDeleted(treeItem: AccountsAccountTreeItem) {
        val account = treeItem.bank

        val selectedButton = JavaFxDialogService().showDialog(
            Alert.AlertType.WARNING,
            String.format(messages["accounts.view.ask.really.delete.account"], account.displayName),
            null, FX.primaryStage, ButtonType.YES, ButtonType.NO)

        if (selectedButton == ButtonType.YES) {
            presenter.deleteAccount(account)
        }
    }

}
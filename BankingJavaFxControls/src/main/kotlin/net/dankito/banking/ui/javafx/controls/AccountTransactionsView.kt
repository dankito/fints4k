package net.dankito.banking.ui.javafx.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.ContextMenu
import javafx.scene.input.ContextMenuEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.GetTransactionsResponse
import net.dankito.banking.ui.presenter.MainWindowPresenter
import tornadofx.*


open class AccountTransactionsView(private val presenter: MainWindowPresenter) : View() {

    protected val isAccountSelected = SimpleBooleanProperty(false)

    protected val transactionsFilter = SimpleStringProperty("")

    protected val balance = SimpleStringProperty("")

    protected val transactionsToDisplay = FXCollections.observableArrayList<AccountTransaction>(presenter.allTransactions)


    protected var currentMenu: ContextMenu? = null


    init {
        presenter.addAccountAddedListener { handleAccountAdded() }

        presenter.addRetrievedAccountTransactionsResponseListener { _, response ->
            handleGetTransactionsResponseOffUiThread(response)
        }

        transactionsFilter.addListener { _, _, newValue ->
            transactionsToDisplay.setAll(presenter.searchAccountTransactions(newValue))
        }
    }


    override val root = vbox {
        add(AccountTransactionsControlView(presenter, transactionsFilter, balance).apply {
            enableWhen(isAccountSelected)
        })

        add(AccountTransactionsTable(presenter, transactionsToDisplay).apply {
            setOnMouseClicked { tableClicked(it, this.selectionModel.selectedItem) }

            setOnContextMenuRequested { event -> showContextMenu(event, this) }
        })
    }


    protected open fun tableClicked(event: MouseEvent, selectedItem: AccountTransaction?) {
        if (event.button == MouseButton.PRIMARY || event.button == MouseButton.MIDDLE) {
            currentMenu?.hide()
        }

        if(event.clickCount == 2 && event.button == MouseButton.PRIMARY) {
            if(selectedItem != null) {
                showTransactionDetailsDialog(selectedItem)
            }
        }
    }

    protected open fun showContextMenu(event: ContextMenuEvent, table: AccountTransactionsTable) {
        currentMenu?.hide()

        val selectedItem = table.selectionModel.selectedItem

        if (selectedItem != null) {
            currentMenu = createContextMenuForItems(selectedItem)
            currentMenu?.show(table, event.screenX, event.screenY)
        }
    }

    protected open fun createContextMenuForItems(selectedItem: AccountTransaction): ContextMenu {
        val contextMenu = ContextMenu()

        contextMenu.apply {
            if (selectedItem.otherPartyName != null) {
                item(String.format(FX.messages["account.transactions.table.context.menu.transfer.money.to"], selectedItem.otherPartyName)) {
                    action { showTransferMoneyDialog(selectedItem) }
                }

                separator()
            }

            item(FX.messages["account.transactions.table.context.menu.show.transaction.details"]) {
                action {
                    showTransactionDetailsDialog(selectedItem)
                }
            }
        }

        return contextMenu
    }

    protected open fun showTransactionDetailsDialog(transaction: AccountTransaction) {
        // TODO:
//        presenter.showTransactionDetailsWindow(transaction.item)
    }

    protected open fun showTransferMoneyDialog(transaction: AccountTransaction) {
        presenter.showTransferMoneyDialog(transaction.bankAccount, TransferMoneyData.fromAccountTransaction(transaction))
    }


    protected open fun handleAccountAdded() {
        isAccountSelected.value = presenter.accounts.isNotEmpty() // TODO: not correct, check if an account has been selected
    }

    protected open fun handleGetTransactionsResponseOffUiThread(response: GetTransactionsResponse) {
        runLater { handleGetTransactionsResponseOnUiThread(response) }
    }

    protected open fun handleGetTransactionsResponseOnUiThread(response: GetTransactionsResponse) {
        if (response.isSuccessful) {
            transactionsToDisplay.setAll(presenter.allTransactions) // TODO: get that one for currently displayed account and apply current filter on it

            // TODO: if transactions are filtered calculate and show balance of displayed transactions?
            balance.value = presenter.balanceOfAllAccounts.toString() // TODO: get that one for currently displayed account and add currency
        }
    }

}

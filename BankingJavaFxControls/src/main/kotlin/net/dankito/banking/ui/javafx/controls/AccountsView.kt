package net.dankito.banking.ui.javafx.controls

import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.TreeItem
import javafx.scene.layout.Priority
import net.dankito.banking.ui.javafx.model.AccountsAccountTreeItem
import net.dankito.banking.ui.javafx.model.AccountsBankAccountTreeItem
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.javafx.ui.controls.addButton
import net.dankito.utils.javafx.ui.extensions.fixedHeight
import net.dankito.utils.javafx.ui.extensions.fixedWidth
import tornadofx.*


open class AccountsView(protected val presenter: BankingPresenter) : View() {

    protected val accounts = FXCollections.observableArrayList(presenter.accounts)


    init {
        presenter.addAccountsChangedListener {
            runLater {
                accounts.setAll(it)
            }
        }
    }


    override val root = vbox {
        borderpane {
            fixedHeight = 36.0

            left = label(messages["accounts"]) {
                borderpaneConstraints {
                    alignment = Pos.CENTER_LEFT
                    marginLeft = 4.0
                }
            }

            right = addButton(fontSize = 14.0) {
                fixedHeight = 32.0
                fixedWidth = 32.0

                action { showAddAccountDialog() }

                borderpaneConstraints {
                    alignment = Pos.CENTER_RIGHT
                    marginTopBottom(2.0)
                }
            }
        }

        add(AccountsTreeView(accounts).apply {
            selectionModel.selectedItemProperty().addListener { _, _, newValue -> selectedBankAccountChanged(newValue) }

            vboxConstraints {
                vGrow = Priority.ALWAYS
            }
        })

    }


    protected open fun showAddAccountDialog() {
        presenter.showAddAccountDialog()
    }

    protected open fun selectedBankAccountChanged(accountTreeItem: TreeItem<String>?) {
        accountTreeItem?.let {
            when (accountTreeItem) {
                is AccountsBankAccountTreeItem -> presenter.selectedBankAccount(accountTreeItem.bankAccount)
                is AccountsAccountTreeItem -> presenter.selectedAccount(accountTreeItem.account)
                else -> presenter.selectedAllBankAccounts()
            }
        }
    }

}
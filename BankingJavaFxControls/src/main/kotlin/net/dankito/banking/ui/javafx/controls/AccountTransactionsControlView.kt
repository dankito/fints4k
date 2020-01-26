package net.dankito.banking.ui.javafx.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import net.dankito.banking.ui.presenter.MainWindowPresenter
import net.dankito.utils.javafx.ui.controls.UpdateButton
import net.dankito.utils.javafx.ui.controls.addButton
import net.dankito.utils.javafx.ui.controls.searchtextfield
import net.dankito.utils.javafx.ui.controls.updateButton
import net.dankito.utils.javafx.ui.extensions.fixedHeight
import tornadofx.*


open class AccountTransactionsControlView(
    protected val presenter: MainWindowPresenter,
    protected val transactionsFilter: SimpleStringProperty,
    protected val balance: SimpleStringProperty
) : View() {


    protected val supportsRetrievingAccountTransactions = SimpleBooleanProperty(false)

    protected val supportsTransferringMoney = SimpleBooleanProperty(false)
    
    
    override val root = borderpane {
        fixedHeight = 36.0

        left = label(messages["account.transactions.control.view.search.label"]) {
            borderpaneConstraints {
                alignment = Pos.CENTER_LEFT
                margin = Insets(4.0, 12.0, 4.0, 4.0)
            }
        }

        center {
            searchtextfield(transactionsFilter) {
                enableWhen(supportsRetrievingAccountTransactions)
            }
        }

        right = hbox {
            alignment = Pos.CENTER_LEFT

            hbox {
                useMaxHeight = true

                visibleWhen(supportsRetrievingAccountTransactions)

                label(messages["account.transactions.control.view.balance.label"]) {
                    hboxConstraints {
                        alignment = Pos.CENTER_LEFT
                        marginRight = 6.0
                    }
                }

                label(balance) {
                    minWidth = 50.0
                    alignment = Pos.CENTER_RIGHT
                }

                hboxConstraints {
                    alignment = Pos.CENTER_LEFT
                    marginLeft = 48.0
                }
            }

            updateButton {
                enableWhen(supportsRetrievingAccountTransactions)

                action { updateAccountTransactions(this) }

                hboxConstraints {
                    marginLeft = 12.0
                }
            }

            addButton {
                useMaxHeight = true

                enableWhen(supportsTransferringMoney)

                action { presenter.showTransferMoneyDialog() }

                hboxConstraints {
                    marginLeft = 12.0
                }
            }
        }

        initLogic()
    }


    protected open fun initLogic() {
        presenter.addAccountsChangedListener { accountsChanged() }
        presenter.addSelectedBankAccountsChangedListener { selectedBankAccountsChanged() }

        checkIfSupportsTransferringMoneyOnUiThread()
        checkIfSupportsRetrievingAccountTransactionsOnUiThread()
    }

    protected open fun accountsChanged() {
        runLater {
            checkIfSupportsTransferringMoneyOnUiThread()
        }
    }

    protected open fun selectedBankAccountsChanged() {
        runLater {
            checkIfSupportsRetrievingAccountTransactionsOnUiThread()
        }
    }


    protected open fun checkIfSupportsTransferringMoneyOnUiThread() {
        supportsTransferringMoney.value = presenter.hasBankAccountsSupportTransferringMoney
    }

    protected open fun checkIfSupportsRetrievingAccountTransactionsOnUiThread() {
        supportsRetrievingAccountTransactions.value = presenter.doSelectedBankAccountsSupportRetrievingAccountTransactions
    }

    protected open fun updateAccountTransactions(updateButton: UpdateButton) {
        presenter.updateAccountsTransactionsAsync { transactions ->
            runLater {
                updateButton.resetIsUpdating()
            }
        }
    }

}
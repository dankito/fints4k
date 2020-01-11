package net.dankito.banking.ui.javafx.dialogs.mainwindow.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.presenter.MainWindowPresenter
import net.dankito.utils.javafx.ui.extensions.fixedHeight
import tornadofx.*


open class MainMenuBar(protected val presenter: MainWindowPresenter) : View() {

    protected val areAccountsThatCanTransferMoneyAdded = SimpleBooleanProperty()


    init {
        presenter.addAccountsChangedListener {
            checkIfThereAreAccountsThatCanTransferMoney(it)
        }

        checkIfThereAreAccountsThatCanTransferMoney(presenter.accounts)
    }


    override val root =
        menubar {
            fixedHeight = 30.0

            menu(messages["main.window.menu.file"]) {
                menu(messages["main.window.menu.file.new"]) {
                    item(messages["main.window.menu.file.new.account"], KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN)) {
                        action { presenter.showAddAccountDialog() }
                    }

                    item(messages["main.window.menu.file.new.cash.transfer"], KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN)) {
                        enableWhen(areAccountsThatCanTransferMoneyAdded)

                        action { presenter.showTransferMoneyDialog() }
                    }
                }

                separator()

                item(messages["main.window.menu.file.quit"], KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN)) {
                    action { primaryStage.close() }
                }
            }
        }


    protected open fun checkIfThereAreAccountsThatCanTransferMoney(accounts: List<Account>) {
        areAccountsThatCanTransferMoneyAdded.value = accounts.isNotEmpty() // TODO: add check if they support transferring money
    }

}
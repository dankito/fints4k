package net.dankito.banking.fints4java.android.ui.views

import android.support.v7.app.AppCompatActivity
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import kotlinx.android.synthetic.main.view_floating_action_button_main.view.*
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.presenter.MainWindowPresenter


open class MainActivityFloatingActionMenuButton(floatingActionMenu: FloatingActionMenu, protected val presenter: MainWindowPresenter)
    : FloatingActionMenuButton(floatingActionMenu) {

    protected lateinit var fabTransferMoney: FloatingActionButton

    init {
        setupButtons(floatingActionMenu)

        presenter.addAccountsChangedListener {
            checkIfThereAreAccountsThatCanTransferMoney(it)
        }

        checkIfThereAreAccountsThatCanTransferMoney(presenter.accounts)
    }

    private fun setupButtons(floatingActionMenu: FloatingActionMenu) {
        (floatingActionMenu.context as? AppCompatActivity)?.let { activity ->
            floatingActionMenu.fabAddAccount.setOnClickListener {
                executeAndCloseMenu { presenter.showAddAccountDialog() }
            }

            fabTransferMoney = floatingActionMenu.fabTransferMoney

            fabTransferMoney.setOnClickListener {
                executeAndCloseMenu { presenter.showTransferMoneyDialog() }
            }
        }
    }


    protected open fun checkIfThereAreAccountsThatCanTransferMoney(accounts: List<Account>) {
        fabTransferMoney.isEnabled = accounts.isNotEmpty() // TODO: add check if they support transferring money
    }

}
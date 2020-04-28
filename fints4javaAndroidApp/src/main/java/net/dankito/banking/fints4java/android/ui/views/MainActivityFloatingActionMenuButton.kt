package net.dankito.banking.fints4java.android.ui.views

import androidx.appcompat.app.AppCompatActivity
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import kotlinx.android.synthetic.main.view_floating_action_button_main.view.*
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.android.extensions.asActivity


open class MainActivityFloatingActionMenuButton(floatingActionMenu: FloatingActionMenu, protected val presenter: BankingPresenter)
    : FloatingActionMenuButton(floatingActionMenu) {

    protected lateinit var fabTransferMoney: FloatingActionButton

    init {
        setupButtons(floatingActionMenu)

        presenter.addAccountsChangedListener {
            fabTransferMoney.context.asActivity()?.runOnUiThread {
                checkIfThereAreAccountsThatCanTransferMoney()
            }
        }

        checkIfThereAreAccountsThatCanTransferMoney()
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


    protected open fun checkIfThereAreAccountsThatCanTransferMoney() {
        fabTransferMoney.isEnabled = presenter.hasBankAccountsSupportTransferringMoney
    }

}
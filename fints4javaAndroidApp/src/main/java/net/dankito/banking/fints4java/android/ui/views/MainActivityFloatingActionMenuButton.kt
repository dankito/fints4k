package net.dankito.banking.fints4java.android.ui.views

import android.support.v7.app.AppCompatActivity
import com.github.clans.fab.FloatingActionMenu
import kotlinx.android.synthetic.main.view_floating_action_button_main.view.*
import net.dankito.banking.ui.presenter.MainWindowPresenter
import net.dankito.banking.fints4java.android.ui.dialogs.AddAccountDialog
import net.dankito.banking.fints4java.android.ui.dialogs.TransferMoneyDialog


open class MainActivityFloatingActionMenuButton(floatingActionMenu: FloatingActionMenu, protected val presenter: MainWindowPresenter)
    : FloatingActionMenuButton(floatingActionMenu) {

    init {
        setupButtons(floatingActionMenu)
    }

    private fun setupButtons(floatingActionMenu: FloatingActionMenu) {
        (floatingActionMenu.context as? AppCompatActivity)?.let { activity ->
            floatingActionMenu.fabAddAccount.setOnClickListener {
                executeAndCloseMenu { AddAccountDialog().show(activity, presenter) }
            }

            val fabTransferMoney = floatingActionMenu.fabTransferMoney
            fabTransferMoney.isEnabled = false

            floatingActionMenu.setOnMenuToggleListener {
                if (floatingActionMenu.isOpened) {
                    fabTransferMoney.isEnabled = presenter.accounts.isNotEmpty()
                }
            }

            fabTransferMoney.setOnClickListener {
                executeAndCloseMenu { TransferMoneyDialog().show(activity, presenter, null) }
            }
        }
    }

}
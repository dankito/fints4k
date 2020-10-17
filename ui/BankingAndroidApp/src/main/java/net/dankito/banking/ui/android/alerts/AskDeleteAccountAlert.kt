package net.dankito.banking.ui.android.alerts

import android.content.Context
import androidx.appcompat.app.AlertDialog
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.model.TypedBankData
import net.dankito.banking.ui.presenter.BankingPresenter


open class AskDeleteAccountAlert {

    open fun show(bank: TypedBankData, presenter: BankingPresenter, context: Context, accountDeleted: (() -> Unit)? = null) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.alert_ask_delete_account_title))
            .setMessage(context.getString(R.string.alert_ask_delete_account_message, bank.displayName))
            .setPositiveButton(R.string.delete) { dialog, _ ->
                presenter.deleteAccount(bank)
                dialog.dismiss()
                accountDeleted?.invoke()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

}
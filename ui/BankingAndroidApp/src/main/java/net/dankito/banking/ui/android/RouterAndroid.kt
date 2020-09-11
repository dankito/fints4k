package net.dankito.banking.ui.android

import net.dankito.banking.ui.android.util.CurrentActivityTracker
import net.dankito.banking.ui.IRouter
import net.dankito.banking.ui.android.dialogs.*
import net.dankito.banking.ui.model.TypedCustomer
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium
import net.dankito.banking.ui.presenter.BankingPresenter


open class RouterAndroid(protected val activityTracker: CurrentActivityTracker) : IRouter {

    override fun showAddAccountDialog(presenter: BankingPresenter) {
        activityTracker.currentOrNextActivity { activity ->
            AddAccountDialog().show(activity)
        }
    }

    override fun getTanFromUserFromNonUiThread(customer: TypedCustomer, tanChallenge: TanChallenge, presenter: BankingPresenter, callback: (EnterTanResult) -> Unit) {
       activityTracker.currentOrNextActivity { activity ->
            activity.runOnUiThread {
                EnterTanDialog().show(customer, tanChallenge, activity, false) { result ->
                    callback(result)
                }
            }
        }
    }

    override fun getAtcFromUserFromNonUiThread(tanMedium: TanGeneratorTanMedium, callback: (EnterTanGeneratorAtcResult) -> Unit) {
        activityTracker.currentOrNextActivity { activity ->
            activity.runOnUiThread {
                EnterAtcDialog().show(tanMedium, activity, false) { enteredResult ->
                    callback(enteredResult)
                }
            }
        }
    }

    override fun showTransferMoneyDialog(presenter: BankingPresenter, preselectedValues: TransferMoneyData?) {
        activityTracker.currentOrNextActivity { activity ->
            TransferMoneyDialog().show(activity, preselectedValues)
        }
    }

    override fun showSendMessageLogDialog(presenter: BankingPresenter) {
        activityTracker.currentOrNextActivity { activity ->
            activity.runOnUiThread {
                SendMessageLogDialog().show(activity)
            }
        }
    }

}
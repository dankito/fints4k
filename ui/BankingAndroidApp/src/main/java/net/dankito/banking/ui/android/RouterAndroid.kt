package net.dankito.banking.ui.android

import net.dankito.banking.ui.android.util.CurrentActivityTracker
import net.dankito.banking.ui.IRouter
import net.dankito.banking.ui.android.dialogs.*
import net.dankito.banking.ui.model.Customer
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium
import net.dankito.banking.ui.presenter.BankingPresenter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference


open class RouterAndroid(protected val activityTracker: CurrentActivityTracker) : IRouter {

    override fun showAddAccountDialog(presenter: BankingPresenter) {
        activityTracker.currentOrNextActivity { activity ->
            AddAccountDialog().show(activity)
        }
    }

    override fun getTanFromUserFromNonUiThread(customer: Customer, tanChallenge: TanChallenge, presenter: BankingPresenter): EnterTanResult {
        val enteredTan = AtomicReference<EnterTanResult>(null)
        val tanEnteredLatch = CountDownLatch(1)

        activityTracker.currentOrNextActivity { activity ->
            activity.runOnUiThread {
                EnterTanDialog().show(customer, tanChallenge, activity, false) {
                    enteredTan.set(it)
                    tanEnteredLatch.countDown()
                }
            }
        }

        try { tanEnteredLatch.await() } catch (ignored: Exception) { }

        return enteredTan.get()
    }

    override fun getAtcFromUserFromNonUiThread(tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
        val result = AtomicReference<EnterTanGeneratorAtcResult>(null)
        val tanEnteredLatch = CountDownLatch(1)

        activityTracker.currentOrNextActivity { activity ->
            activity.runOnUiThread {
                EnterAtcDialog().show(tanMedium, activity, false) { enteredResult ->
                    result.set(enteredResult)
                    tanEnteredLatch.countDown()
                }
            }
        }

        try { tanEnteredLatch.await() } catch (ignored: Exception) { }

        return result.get()
    }

    override fun showTransferMoneyDialog(presenter: BankingPresenter, preselectedBankAccount: BankAccount?, preselectedValues: TransferMoneyData?) {
        activityTracker.currentOrNextActivity { activity ->
            TransferMoneyDialog().show(activity, preselectedBankAccount, preselectedValues)
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
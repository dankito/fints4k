package net.dankito.banking.fints4java.android

import net.dankito.banking.fints4java.android.ui.dialogs.AddAccountDialog
import net.dankito.banking.fints4java.android.ui.dialogs.EnterAtcDialog
import net.dankito.banking.fints4java.android.ui.dialogs.EnterTanDialog
import net.dankito.banking.fints4java.android.ui.dialogs.TransferMoneyDialog
import net.dankito.banking.fints4java.android.ui.util.CurrentActivityTracker
import net.dankito.banking.ui.IRouter
import net.dankito.banking.ui.model.Account
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
        activityTracker.currentActivity?.let { activity ->
            AddAccountDialog().show(activity)
        }
    }

    override fun getTanFromUserFromNonUiThread(account: Account, tanChallenge: TanChallenge, presenter: BankingPresenter): EnterTanResult {
        return activityTracker.currentActivity?.let { activity ->
            val enteredTan = AtomicReference<EnterTanResult>(null)
            val tanEnteredLatch = CountDownLatch(1)

            activity.runOnUiThread {
                EnterTanDialog().show(account, tanChallenge, activity, false) {
                    enteredTan.set(it)
                    tanEnteredLatch.countDown()
                }
            }

            try { tanEnteredLatch.await() } catch (ignored: Exception) { }

            enteredTan.get()
        }
        ?: EnterTanResult.userDidNotEnterTan()
    }

    override fun getAtcFromUserFromNonUiThread(tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
        return activityTracker.currentActivity?.let { activity ->
            val result = AtomicReference<EnterTanGeneratorAtcResult>(null)
            val tanEnteredLatch = CountDownLatch(1)

            activity.runOnUiThread {
                EnterAtcDialog().show(tanMedium, activity, false) { enteredResult ->
                    result.set(enteredResult)
                    tanEnteredLatch.countDown()
                }
            }

            try { tanEnteredLatch.await() } catch (ignored: Exception) { }

            result.get()
        }
        ?: EnterTanGeneratorAtcResult.userDidNotEnterTan()
    }

    override fun showTransferMoneyDialog(presenter: BankingPresenter, preselectedBankAccount: BankAccount?, preselectedValues: TransferMoneyData?) {
        activityTracker.currentActivity?.let { activity ->
            TransferMoneyDialog().show(activity, preselectedBankAccount, preselectedValues)
        }
    }

}
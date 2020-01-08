package net.dankito.banking.fints4java.android

import android.support.v7.app.AppCompatActivity
import net.dankito.banking.fints4java.android.ui.dialogs.EnterAtcDialog
import net.dankito.banking.fints4java.android.ui.dialogs.EnterTanDialog
import net.dankito.banking.ui.IRouter
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium
import net.dankito.banking.ui.presenter.MainWindowPresenter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference


open class RouterAndroid(protected val activity: AppCompatActivity) : IRouter {

    override fun getTanFromUserOffUiThread(account: Account, tanChallenge: TanChallenge, presenter: MainWindowPresenter): EnterTanResult {
        val enteredTan = AtomicReference<EnterTanResult>(null)
        val tanEnteredLatch = CountDownLatch(1)

        activity.runOnUiThread {
            EnterTanDialog().show(account, tanChallenge, presenter, activity, false) {
                enteredTan.set(it)
                tanEnteredLatch.countDown()
            }
        }

        try { tanEnteredLatch.await() } catch (ignored: Exception) { }

        return enteredTan.get()
    }

    override fun getAtcFromUserOffUiThread(tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
        val result = AtomicReference<EnterTanGeneratorAtcResult>(null)
        val tanEnteredLatch = CountDownLatch(1)

        activity.runOnUiThread {
            EnterAtcDialog().show(tanMedium, activity, false) { enteredResult ->
                result.set(enteredResult)
                tanEnteredLatch.countDown()
            }
        }

        try { tanEnteredLatch.await() } catch (ignored: Exception) { }

        return result.get()
    }

}
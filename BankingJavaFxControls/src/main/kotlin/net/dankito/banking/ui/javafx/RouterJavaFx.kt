package net.dankito.banking.ui.javafx

import net.dankito.banking.ui.IRouter
import net.dankito.banking.ui.javafx.dialogs.AddAccountDialog
import net.dankito.banking.ui.javafx.dialogs.tan.EnterTanDialog
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium
import net.dankito.banking.ui.presenter.MainWindowPresenter
import tornadofx.FX
import tornadofx.FX.Companion.messages
import tornadofx.get
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference


open class RouterJavaFx : IRouter {

    override fun showAddAccountDialog(presenter: MainWindowPresenter) {
        AddAccountDialog(presenter).show(messages["add.account.dialog.title"])
    }

    override fun getTanFromUserFromNonUiThread(account: Account, tanChallenge: TanChallenge, presenter: MainWindowPresenter): EnterTanResult {
        val enteredTan = AtomicReference<EnterTanResult>(null)
        val tanEnteredLatch = CountDownLatch(1)

        FX.runAndWait {
            EnterTanDialog(account, tanChallenge, presenter) {
                enteredTan.set(it)
                tanEnteredLatch.countDown()
            }.show()
        }

        try { tanEnteredLatch.await() } catch (ignored: Exception) { }

        return enteredTan.get()
    }

    override fun getAtcFromUserFromNonUiThread(tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
        return EnterTanGeneratorAtcResult.userDidNotEnterTan()
    }

}
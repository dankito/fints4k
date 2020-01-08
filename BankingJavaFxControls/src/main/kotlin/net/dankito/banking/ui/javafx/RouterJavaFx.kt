package net.dankito.banking.ui.javafx

import net.dankito.banking.ui.IRouter
import net.dankito.banking.ui.javafx.dialogs.AddAccountDialog
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium
import net.dankito.banking.ui.presenter.MainWindowPresenter
import tornadofx.FX.Companion.messages
import tornadofx.get


open class RouterJavaFx : IRouter {

    override fun showAddAccountDialog(presenter: MainWindowPresenter) {
        AddAccountDialog(presenter).show(messages["add.account.dialog.title"])
    }

    override fun getTanFromUserFromNonUiThread(account: Account, tanChallenge: TanChallenge, presenter: MainWindowPresenter): EnterTanResult {
        return EnterTanResult.userDidNotEnterTan()
    }

    override fun getAtcFromUserFromNonUiThread(tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
        return EnterTanGeneratorAtcResult.userDidNotEnterTan()
    }

}
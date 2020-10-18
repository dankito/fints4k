package net.dankito.banking.ui.javafx

import net.dankito.banking.ui.IRouter
import net.dankito.banking.ui.javafx.dialogs.AddAccountDialog
import net.dankito.banking.ui.javafx.dialogs.cashtransfer.TransferMoneyDialog
import net.dankito.banking.ui.javafx.dialogs.tan.EnterTanDialog
import net.dankito.banking.ui.model.TypedBankData
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium
import net.dankito.banking.ui.presenter.BankingPresenter
import tornadofx.FX
import tornadofx.FX.Companion.messages
import tornadofx.get


open class RouterJavaFx : IRouter {

    override fun showAddAccountDialog(presenter: BankingPresenter) {
        AddAccountDialog(presenter).show(messages["add.account.dialog.title"])
    }

    override fun getTanFromUserFromNonUiThread(bank: TypedBankData, tanChallenge: TanChallenge, presenter: BankingPresenter, callback: (EnterTanResult) -> Unit) {
        FX.runAndWait {
            EnterTanDialog(bank, tanChallenge, presenter) { result ->
                callback(result)
            }.show(messages["enter.tan.dialog.title"])
        }
    }

    override fun getAtcFromUserFromNonUiThread(tanMedium: TanGeneratorTanMedium, callback: (EnterTanGeneratorAtcResult) -> Unit) {
        callback(EnterTanGeneratorAtcResult.userDidNotEnterAtc()) // TODO: implement EnterAtcDialog
    }

    override fun showTransferMoneyDialog(presenter: BankingPresenter, preselectedValues: TransferMoneyData?) {
        TransferMoneyDialog(presenter, preselectedValues).show(messages["transfer.money.dialog.title"])
    }

    override fun showSendMessageLogDialog(presenter: BankingPresenter) {
        // TODO
    }

}
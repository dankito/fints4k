package net.dankito.banking.ui

import net.dankito.banking.ui.model.TypedBankData
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium
import net.dankito.banking.ui.presenter.BankingPresenter


interface IRouter {

    fun showAddAccountDialog(presenter: BankingPresenter)

    fun getTanFromUserFromNonUiThread(bank: TypedBankData, tanChallenge: TanChallenge, presenter: BankingPresenter, callback: (EnterTanResult) -> Unit)

    fun getAtcFromUserFromNonUiThread(tanMedium: TanGeneratorTanMedium, callback: (EnterTanGeneratorAtcResult) -> Unit)

    fun showTransferMoneyDialog(presenter: BankingPresenter, preselectedValues: TransferMoneyData?)

    fun showSendMessageLogDialog(presenter: BankingPresenter)

}
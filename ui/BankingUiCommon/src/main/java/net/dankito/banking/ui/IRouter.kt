package net.dankito.banking.ui

import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium
import net.dankito.banking.ui.presenter.BankingPresenter


interface IRouter {

    fun showAddAccountDialog(presenter: BankingPresenter)

    fun getTanFromUserFromNonUiThread(account: Account, tanChallenge: TanChallenge, presenter: BankingPresenter): EnterTanResult

    fun getAtcFromUserFromNonUiThread(tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult

    fun showTransferMoneyDialog(presenter: BankingPresenter, preselectedBankAccount: BankAccount?, preselectedValues: TransferMoneyData?)

    fun showSendMessageLogDialog(presenter: BankingPresenter)

}
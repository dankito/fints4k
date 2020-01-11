package net.dankito.banking.ui

import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium
import net.dankito.banking.ui.presenter.MainWindowPresenter


interface IRouter {

    fun showAddAccountDialog(presenter: MainWindowPresenter)

    fun getTanFromUserFromNonUiThread(account: Account, tanChallenge: TanChallenge, presenter: MainWindowPresenter): EnterTanResult

    fun getAtcFromUserFromNonUiThread(tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult

    fun showTransferMoneyDialog(presenter: MainWindowPresenter, preselectedBankAccount: BankAccount?, preselectedValues: TransferMoneyData?)

}
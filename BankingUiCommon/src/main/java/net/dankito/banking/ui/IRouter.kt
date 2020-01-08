package net.dankito.banking.ui

import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium


interface IRouter {

    fun getTanFromUserOffUiThread(account: Account, tanChallenge: TanChallenge): EnterTanResult

    fun getAtcFromUserOffUiThread(tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult

}
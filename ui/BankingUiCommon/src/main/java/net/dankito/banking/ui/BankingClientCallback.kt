package net.dankito.banking.ui

import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium


interface BankingClientCallback {

    fun enterTan(account: Account, tanChallenge: TanChallenge): EnterTanResult

    /**
     * This method gets called for chipTan TAN generators when the bank asks the customer to synchronize her/his TAN generator.
     */
    fun enterTanGeneratorAtc(tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult

}
package net.dankito.banking.ui

import net.dankito.banking.ui.model.TypedBankData
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium


interface BankingClientCallback {

    fun enterTan(bank: TypedBankData, tanChallenge: TanChallenge, callback: (EnterTanResult) -> Unit)

    /**
     * This method gets called for chipTan TAN generators when the bank asks the customer to synchronize her/his TAN generator.
     */
    fun enterTanGeneratorAtc(tanMedium: TanGeneratorTanMedium, callback: (EnterTanGeneratorAtcResult) -> Unit)

}
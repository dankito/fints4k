package net.dankito.banking.fints.callback

import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.banking.fints.model.*


open class NoOpFinTsClientCallback : FinTsClientCallback {

    override fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>,
                                        suggestedTanProcedure: TanProcedure?, callback: (TanProcedure?) -> Unit) {

        callback(suggestedTanProcedure)
    }

    override fun enterTan(bank: BankData, tanChallenge: TanChallenge, callback: (EnterTanResult) -> Unit) {
        callback(EnterTanResult.userDidNotEnterTan())
    }

    override fun enterTanGeneratorAtc(bank: BankData, tanMedium: TanGeneratorTanMedium, callback: (EnterTanGeneratorAtcResult) -> Unit) {
        callback(EnterTanGeneratorAtcResult.userDidNotEnterAtc())
    }

}
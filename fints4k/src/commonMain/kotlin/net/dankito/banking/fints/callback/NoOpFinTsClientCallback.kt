package net.dankito.banking.fints.callback

import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.banking.fints.model.*


open class NoOpFinTsClientCallback : FinTsClientCallback {

    override fun askUserForTanMethod(supportedTanMethods: List<TanMethod>,
                                     suggestedTanMethod: TanMethod?, callback: (TanMethod?) -> Unit) {

        callback(suggestedTanMethod)
    }

    override fun enterTan(bank: BankData, tanChallenge: TanChallenge, callback: (EnterTanResult) -> Unit) {
        callback(EnterTanResult.userDidNotEnterTan())
    }

    override fun enterTanGeneratorAtc(bank: BankData, tanMedium: TanGeneratorTanMedium, callback: (EnterTanGeneratorAtcResult) -> Unit) {
        callback(EnterTanGeneratorAtcResult.userDidNotEnterAtc())
    }

}
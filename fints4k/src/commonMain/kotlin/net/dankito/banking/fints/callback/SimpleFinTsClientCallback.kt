package net.dankito.banking.fints.callback

import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.banking.fints.model.*


open class SimpleFinTsClientCallback(
    protected val enterTan: ((bank: BankData, tanChallenge: TanChallenge) -> EnterTanResult)? = null,
    protected val enterTanGeneratorAtc: ((bank: BankData, tanMedium: TanGeneratorTanMedium) -> EnterTanGeneratorAtcResult)? = null,
    protected val askUserForTanProcedure: ((supportedTanProcedures: List<TanProcedure>, suggestedTanProcedure: TanProcedure?) -> TanProcedure?)? = null
) : FinTsClientCallback {

    override fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>,
                                        suggestedTanProcedure: TanProcedure?, callback: (TanProcedure?) -> Unit) {

        callback(askUserForTanProcedure?.invoke(supportedTanProcedures, suggestedTanProcedure) ?: suggestedTanProcedure)
    }

    override fun enterTan(bank: BankData, tanChallenge: TanChallenge, callback: (EnterTanResult) -> Unit) {
        callback(enterTan?.invoke(bank, tanChallenge) ?: EnterTanResult.userDidNotEnterTan())
    }

    override fun enterTanGeneratorAtc(bank: BankData, tanMedium: TanGeneratorTanMedium, callback: (EnterTanGeneratorAtcResult) -> Unit) {
        callback(enterTanGeneratorAtc?.invoke(bank, tanMedium) ?: EnterTanGeneratorAtcResult.userDidNotEnterAtc())
    }

}
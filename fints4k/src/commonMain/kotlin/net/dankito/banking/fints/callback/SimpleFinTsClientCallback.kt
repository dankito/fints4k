package net.dankito.banking.fints.callback

import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.banking.fints.model.*


open class SimpleFinTsClientCallback(
    protected val enterTan: ((customer: CustomerData, tanChallenge: TanChallenge) -> EnterTanResult)? = null,
    protected val enterTanGeneratorAtc: ((customer: CustomerData, tanMedium: TanGeneratorTanMedium) -> EnterTanGeneratorAtcResult)? = null,
    protected val askUserForTanProcedure: ((supportedTanProcedures: List<TanProcedure>, suggestedTanProcedure: TanProcedure?) -> TanProcedure?)? = null
) : FinTsClientCallback {

    override fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>,
                                        suggestedTanProcedure: TanProcedure?, callback: (TanProcedure?) -> Unit) {

        callback(askUserForTanProcedure?.invoke(supportedTanProcedures, suggestedTanProcedure) ?: suggestedTanProcedure)
    }

    override fun enterTan(customer: CustomerData, tanChallenge: TanChallenge, callback: (EnterTanResult) -> Unit) {
        callback(enterTan?.invoke(customer, tanChallenge) ?: EnterTanResult.userDidNotEnterTan())
    }

    override fun enterTanGeneratorAtc(customer: CustomerData, tanMedium: TanGeneratorTanMedium, callback: (EnterTanGeneratorAtcResult) -> Unit) {
        callback(enterTanGeneratorAtc?.invoke(customer, tanMedium) ?: EnterTanGeneratorAtcResult.userDidNotEnterAtc())
    }

}
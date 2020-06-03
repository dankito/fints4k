package net.dankito.banking.fints.callback

import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.banking.fints.model.*


open class SimpleFinTsClientCallback(
    protected val enterTan: ((customer: CustomerData, tanChallenge: TanChallenge) -> EnterTanResult)? = null,
    protected val enterTanGeneratorAtc: ((customer: CustomerData, tanMedium: TanGeneratorTanMedium) -> EnterTanGeneratorAtcResult)? = null,
    protected val askUserForTanProcedure: ((supportedTanProcedures: List<TanProcedure>, suggestedTanProcedure: TanProcedure?) -> TanProcedure?)? = null
) : FinTsClientCallback {

    override fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>,
                                        suggestedTanProcedure: TanProcedure?): TanProcedure? {

        return askUserForTanProcedure?.invoke(supportedTanProcedures, suggestedTanProcedure) ?: suggestedTanProcedure
    }

    override fun enterTan(customer: CustomerData, tanChallenge: TanChallenge): EnterTanResult {
        return enterTan?.invoke(customer, tanChallenge) ?: EnterTanResult.userDidNotEnterTan()
    }

    override fun enterTanGeneratorAtc(customer: CustomerData, tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
        return enterTanGeneratorAtc?.invoke(customer, tanMedium) ?: EnterTanGeneratorAtcResult.userDidNotEnterAtc()
    }

}
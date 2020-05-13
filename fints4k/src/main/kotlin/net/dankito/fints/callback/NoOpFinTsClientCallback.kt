package net.dankito.fints.callback

import net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.fints.model.*


open class NoOpFinTsClientCallback : FinTsClientCallback {

    override fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>,
                                        suggestedTanProcedure: TanProcedure?): TanProcedure? {

        return suggestedTanProcedure
    }

    override fun enterTan(customer: CustomerData, tanChallenge: TanChallenge): EnterTanResult {
        return EnterTanResult.userDidNotEnterTan()
    }

    override fun enterTanGeneratorAtc(customer: CustomerData, tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
        return EnterTanGeneratorAtcResult.userDidNotEnterAtc()
    }

}
package net.dankito.fints

import net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.fints.model.*


interface FinTsClientCallback {

    fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>): TanProcedure?

    fun enterTan(customer: CustomerData, tanChallenge: TanChallenge): EnterTanResult

    /**
     * This method gets called for chipTan TAN generators when the bank asks the customer to synchronize her/his TAN generator.
     */
    fun enterTanGeneratorAtc(customer: CustomerData, tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult?

}
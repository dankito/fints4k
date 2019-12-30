package net.dankito.fints

import net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.fints.model.CustomerData
import net.dankito.fints.model.EnterTanGeneratorAtcResult
import net.dankito.fints.model.TanChallenge
import net.dankito.fints.model.TanProcedure


interface FinTsClientCallback {

    fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>): TanProcedure?

    fun enterTan(customer: CustomerData, tanChallenge: TanChallenge): String?

    /**
     * This method gets called for chipTan TAN generators when the bank asks the customer to synchronize her/his TAN generator.
     */
    fun enterTanGeneratorAtc(customer: CustomerData, tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult?

}
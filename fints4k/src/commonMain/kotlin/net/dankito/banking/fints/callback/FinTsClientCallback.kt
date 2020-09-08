package net.dankito.banking.fints.callback

import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.banking.fints.model.*


interface FinTsClientCallback {

    /**
     * When user did not select a TAN procedure, this method gets called so that user selects one.
     *
     * As almost all FinTS messages need the selected TAN procedure, this method gets called quite early.
     *
     * As a simplification fints4k already suggests which TAN procedure may is the best one for user.
     *
     * If you do not support an enter tan dialog or if your enter tan dialog supports selecting a TAN procedure, it's
     * best returning [suggestedTanProcedure] and to not show an extra select TAN procedure dialog.
     */
    fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>, suggestedTanProcedure: TanProcedure?, callback: (TanProcedure?) -> Unit)

    fun enterTan(bank: BankData, tanChallenge: TanChallenge, callback: (EnterTanResult) -> Unit)

    /**
     * This method gets called for chipTan TAN generators when the bank asks the customer to synchronize her/his TAN generator.
     *
     * If you do not support entering TAN generator ATC, return [EnterTanGeneratorAtcResult.userDidNotEnterAtc]
     */
    fun enterTanGeneratorAtc(bank: BankData, tanMedium: TanGeneratorTanMedium, callback: (EnterTanGeneratorAtcResult) -> Unit)

}
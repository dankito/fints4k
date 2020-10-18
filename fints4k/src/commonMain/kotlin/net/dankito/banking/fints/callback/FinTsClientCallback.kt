package net.dankito.banking.fints.callback

import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.banking.fints.model.*


interface FinTsClientCallback {

    /**
     * When user did not select a TAN method, this method gets called so that user selects one.
     *
     * As almost all FinTS messages need the selected TAN method, this method gets called quite early.
     *
     * As a simplification fints4k already suggests which TAN method may is the best one for user.
     *
     * If you do not support an enter tan dialog or if your enter tan dialog supports selecting a TAN method, it's
     * best returning [suggestedTanMethod] and to not show an extra select TAN method dialog.
     */
    fun askUserForTanMethod(supportedTanMethods: List<TanMethod>, suggestedTanMethod: TanMethod?, callback: (TanMethod?) -> Unit)

    fun enterTan(bank: BankData, tanChallenge: TanChallenge, callback: (EnterTanResult) -> Unit)

    /**
     * This method gets called for chipTan TAN generators when the bank asks the customer to synchronize her/his TAN generator.
     *
     * If you do not support entering TAN generator ATC, return [EnterTanGeneratorAtcResult.userDidNotEnterAtc]
     */
    fun enterTanGeneratorAtc(bank: BankData, tanMedium: TanGeneratorTanMedium, callback: (EnterTanGeneratorAtcResult) -> Unit)

}
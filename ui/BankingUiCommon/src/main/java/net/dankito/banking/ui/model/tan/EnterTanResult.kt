package net.dankito.banking.ui.model.tan

import net.dankito.banking.ui.model.responses.BankingClientResponse


open class EnterTanResult protected constructor(
    val enteredTan: String?,
    val changeTanProcedureTo: TanProcedure? = null,
    val changeTanMediumTo: TanMedium? = null,
    val changeTanMediumResultCallback: ((BankingClientResponse) -> Unit)? = null
) {

    companion object {

        fun userEnteredTan(enteredTan: String): EnterTanResult {
            return EnterTanResult(enteredTan)
        }

        fun userDidNotEnterTan(): EnterTanResult {
            return EnterTanResult(null)
        }

        fun userAsksToChangeTanProcedure(changeTanProcedureTo: TanProcedure): EnterTanResult {
            return EnterTanResult(null, changeTanProcedureTo)
        }

        fun userAsksToChangeTanMedium(changeTanMediumTo: TanMedium, changeTanMediumResultCallback: ((BankingClientResponse) -> Unit)?): EnterTanResult {
            return EnterTanResult(null, null, changeTanMediumTo, changeTanMediumResultCallback)
        }

    }

    override fun toString(): String {
        if (changeTanProcedureTo != null) {
            return "User asks to change TAN procedure to $changeTanProcedureTo"
        }

        if (changeTanMediumTo != null) {
            return "User asks to change TAN medium to $changeTanMediumTo"
        }

        return "enteredTan = $enteredTan"
    }

}
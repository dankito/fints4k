package net.dankito.banking.ui.model.tan

import net.dankito.banking.ui.model.responses.BankingClientResponse


open class EnterTanResult protected constructor(
    val enteredTan: String?,
    val changeTanMethodTo: TanMethod? = null,
    val changeTanMediumTo: TanMedium? = null,
    val changeTanMediumResultCallback: ((BankingClientResponse) -> Unit)? = null
) {

    companion object {

        fun userEnteredTan(enteredTan: String): EnterTanResult {
            return EnterTanResult(enteredTan.replace(" ", ""))
        }

        fun userDidNotEnterTan(): EnterTanResult {
            return EnterTanResult(null)
        }

        fun userAsksToChangeTanMethod(changeTanMethodTo: TanMethod): EnterTanResult {
            return EnterTanResult(null, changeTanMethodTo)
        }

        fun userAsksToChangeTanMedium(changeTanMediumTo: TanMedium, changeTanMediumResultCallback: ((BankingClientResponse) -> Unit)?): EnterTanResult {
            return EnterTanResult(null, null, changeTanMediumTo, changeTanMediumResultCallback)
        }

    }

    override fun toString(): String {
        if (changeTanMethodTo != null) {
            return "User asks to change TAN method to $changeTanMethodTo"
        }

        if (changeTanMediumTo != null) {
            return "User asks to change TAN medium to $changeTanMediumTo"
        }

        return "enteredTan = $enteredTan"
    }

}
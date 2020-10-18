package net.dankito.banking.fints.model

import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMedium
import net.dankito.banking.fints.response.client.FinTsClientResponse


open class EnterTanResult protected constructor(
    val enteredTan: String?,
    val changeTanMethodTo: TanMethod? = null,
    val changeTanMediumTo: TanMedium? = null,
    val changeTanMediumResultCallback: ((FinTsClientResponse) -> Unit)? = null
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

        fun userAsksToChangeTanMedium(changeTanMediumTo: TanMedium, changeTanMediumResultCallback: ((FinTsClientResponse) -> Unit)?): EnterTanResult {
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
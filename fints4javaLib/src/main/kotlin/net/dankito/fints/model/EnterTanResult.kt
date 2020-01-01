package net.dankito.fints.model

import net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedium
import net.dankito.fints.response.client.FinTsClientResponse


open class EnterTanResult protected constructor(
    val enteredTan: String?,
    val changeTanMediumTo: TanMedium?,
    val changeTanMediumResultCallback: ((FinTsClientResponse) -> Unit)? = null
) {

    companion object {

        fun userEnteredTan(enteredTan: String): EnterTanResult {
            return EnterTanResult(enteredTan, null)
        }

        fun userDidNotEnterTan(): EnterTanResult {
            return EnterTanResult(null, null)
        }

        fun userAsksToChangeTanMedium(changeTanMediumTo: TanMedium, changeTanMediumResultCallback: (FinTsClientResponse) -> Unit): EnterTanResult {
            return EnterTanResult(null, changeTanMediumTo, changeTanMediumResultCallback)
        }

    }

    override fun toString(): String {
        if (changeTanMediumTo != null) {
            return "User asks to change TAN medium to $changeTanMediumTo"
        }

        return "enteredTan = $enteredTan"
    }

}
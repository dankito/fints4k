package net.dankito.fints.model

import net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedium


open class EnterTanResult constructor(
    val enteredTan: String?,
    val changeTanMediumTo: TanMedium?
) {

    companion object {

        fun userEnteredTan(enteredTan: String): EnterTanResult {
            return EnterTanResult(enteredTan, null)
        }

        fun userDidNotEnterTan(): EnterTanResult {
            return EnterTanResult(null, null)
        }

        fun userAsksToChangeTanMedium(changeTanMediumTo: TanMedium): EnterTanResult {
            return EnterTanResult(null, changeTanMediumTo)
        }

    }

    override fun toString(): String {
        if (changeTanMediumTo != null) {
            return "User asks to change TAN medium to $changeTanMediumTo"
        }

        return "enteredTan = $enteredTan"
    }

}
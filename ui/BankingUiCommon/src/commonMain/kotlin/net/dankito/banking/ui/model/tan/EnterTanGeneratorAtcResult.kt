package net.dankito.banking.ui.model.tan


open class EnterTanGeneratorAtcResult protected constructor(
    val tan: String?,
    val atc: Int?
) {

    companion object {

        fun userEnteredAtc(enteredTan: String, enteredAtc: Int): EnterTanGeneratorAtcResult {
            return EnterTanGeneratorAtcResult(enteredTan, enteredAtc)
        }

        fun userDidNotEnterAtc(): EnterTanGeneratorAtcResult {
            return EnterTanGeneratorAtcResult(null, null)
        }

    }


    val hasAtcBeenEntered: Boolean
        get() = tan != null && atc != null


    override fun toString(): String {
        return "TAN: $tan, ATC: $atc"
    }

}
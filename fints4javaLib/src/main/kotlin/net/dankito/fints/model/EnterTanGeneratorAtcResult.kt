package net.dankito.fints.model


open class EnterTanGeneratorAtcResult(
    val tan: String,
    val atc: Int
) {

    override fun toString(): String {
        return "TAN: $tan, ATC: $atc"
    }

}
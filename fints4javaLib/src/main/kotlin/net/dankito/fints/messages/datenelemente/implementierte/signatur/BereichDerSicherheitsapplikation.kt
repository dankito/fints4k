package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.datenelemente.implementierte.ICodeEnum


enum class BereichDerSicherheitsapplikation(val abbreviation: String, override val code: String) : ICodeEnum {

    SignaturkopfUndHBCINutzdaten("SHM", "1"),

    VonSignaturkopfBisSignaturabschluss("SHT", "2")

}
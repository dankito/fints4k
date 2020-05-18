package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur

import net.dankito.banking.fints.messages.datenelemente.implementierte.ICodeEnum


enum class Schluesselart(override val code: String) : ICodeEnum {

    SchluesselZurErzeugungDigitalerSignaturen("D"),

    Signierschluessel("S"),

    Chiffrierschluessel("V")

}
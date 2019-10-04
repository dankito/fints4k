package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.datenelemente.implementierte.ICodeEnum


enum class Sicherheitsverfahren(override val code: String) : ICodeEnum {

    RSA_AES_Hybridverfahren("RAH"),

    PIN_TAN_Verfahren("PIN")

}
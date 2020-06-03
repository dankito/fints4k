package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur

import net.dankito.banking.fints.messages.datenelemente.implementierte.ICodeEnum


enum class Sicherheitsverfahren(override val code: String) : ICodeEnum {

    PIN_TAN_Verfahren("PIN"),

    RSA_AES_Hybridverfahren("RAH"),

    RDH("RDH"),

    DDV("DDV")

}
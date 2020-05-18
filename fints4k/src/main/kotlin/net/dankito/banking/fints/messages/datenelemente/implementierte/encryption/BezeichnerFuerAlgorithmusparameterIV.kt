package net.dankito.banking.fints.messages.datenelemente.implementierte.encryption

import net.dankito.banking.fints.messages.datenelemente.implementierte.ICodeEnum


enum class BezeichnerFuerAlgorithmusparameterIV(override val code: String) : ICodeEnum {

    InitializationValue_ClearText("1")

}
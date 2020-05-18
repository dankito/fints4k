package net.dankito.banking.fints.messages.datenelemente.implementierte.encryption

import net.dankito.banking.fints.messages.datenelemente.implementierte.ICodeEnum


enum class VerwendungDesVerschluesselungsalgorithmus(override val code: String) : ICodeEnum {

    OwnerSymmetric("2")

}
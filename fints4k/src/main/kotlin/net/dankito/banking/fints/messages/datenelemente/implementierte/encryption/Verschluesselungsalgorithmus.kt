package net.dankito.banking.fints.messages.datenelemente.implementierte.encryption

import net.dankito.banking.fints.messages.datenelemente.implementierte.ICodeEnum


enum class Verschluesselungsalgorithmus(override val code: String) : ICodeEnum {

    /**
     * nicht zugelassen
     */
    Two_Key_Triple_DES("13"),
    
    AES_256("14")

}
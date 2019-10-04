package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.datenelemente.implementierte.ICodeEnum


enum class Operationsmodus(override val code: String) : ICodeEnum {

    /**
     * Nur für Verschlüsselung erlaubt (vgl. [HBCI], Kapitel VI.2.2)
     */
    Cipher_Block_Chaining("2"),

    /**
     * nicht zugelassen
     */
    ISO_9796_1("16"),

    /**
     * nicht zugelassen
     */
    ISO_9796_2_mit_Zufallszahl("17"),

    /**
     * nicht zugelassen
     */
    RSASSA_PKCS_1_V1_5("18"),

    /**
     * Nur für Verschlüsselung erlaubt
     */
    RSAES_PKCS_1_V1_5("18"),

    /**
     * Nur für Signatur erlaubt
     */
    RSASSA_PSS("19"),

    /**
     * nicht zugelassen
     */
    Gegenseitig_vereinbart("999")

}
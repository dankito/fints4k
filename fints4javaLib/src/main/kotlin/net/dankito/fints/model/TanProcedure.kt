package net.dankito.fints.model

import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion


open class TanProcedure(
    val displayName: String,
    val securityFunction: Sicherheitsfunktion,
    val type: TanProcedureType
) {

    override fun toString(): String {
        return "$displayName ($type, ${securityFunction.code}"
    }

}
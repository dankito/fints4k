package net.dankito.fints.model

import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion


open class TanProcedure(
    val displayName: String,
    val securityFunction: Sicherheitsfunktion,
    val type: TanProcedureType,
    val hhdVersion: HHDVersion? = null
) {


    internal constructor() : this("", Sicherheitsfunktion.Einschritt_Verfahren, TanProcedureType.EnterTan) // for object deserializers


    override fun toString(): String {
        return "$displayName ($type, ${securityFunction.code})"
    }

}
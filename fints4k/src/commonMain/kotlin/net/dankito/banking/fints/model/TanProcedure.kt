package net.dankito.banking.fints.model

import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.AllowedTanFormat


open class TanProcedure(
    val displayName: String,
    val securityFunction: Sicherheitsfunktion,
    val type: TanProcedureType,
    val hhdVersion: HHDVersion? = null,
    val maxTanInputLength: Int? = null,
    val allowedTanFormat: AllowedTanFormat = AllowedTanFormat.Alphanumeric,
    val nameOfTanMediaRequired: Boolean = false
) {


    internal constructor() : this("", Sicherheitsfunktion.Einschritt_Verfahren, TanProcedureType.EnterTan) // for object deserializers


    override fun toString(): String {
        return "$displayName ($type, ${securityFunction.code})"
    }

}
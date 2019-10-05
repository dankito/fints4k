package net.dankito.fints.messages.datenelementgruppen.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.fints.messages.datenelemente.implementierte.Kreditinstitutscode
import net.dankito.fints.messages.datenelementgruppen.Datenelementgruppe


open class Kreditinstitutskennung @JvmOverloads constructor(
    val bankCountryCode: Int,
    val bankCode: String,
    existenzstatus: Existenzstatus = Existenzstatus.Mandatory
)
    : Datenelementgruppe(listOf(
        Laenderkennzeichen(bankCountryCode, Existenzstatus.Mandatory),
        Kreditinstitutscode(bankCode, Existenzstatus.Mandatory)
    ), existenzstatus)
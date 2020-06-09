package net.dankito.banking.fints.messages.datenelementgruppen.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.banking.fints.messages.datenelemente.implementierte.Kreditinstitutscode
import net.dankito.banking.fints.messages.datenelementgruppen.Datenelementgruppe


open class Kreditinstitutskennung(
    val bankCountryCode: Int,
    val bankCode: String,
    existenzstatus: Existenzstatus = Existenzstatus.Mandatory
)
    : Datenelementgruppe(listOf(
        Laenderkennzeichen(bankCountryCode, Existenzstatus.Mandatory),
        Kreditinstitutscode(bankCode, Existenzstatus.Mandatory)
    ), existenzstatus)
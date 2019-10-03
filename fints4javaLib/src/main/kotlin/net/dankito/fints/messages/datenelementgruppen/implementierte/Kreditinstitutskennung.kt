package net.dankito.fints.messages.datenelementgruppen.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.Kreditinstitutscode
import net.dankito.fints.messages.datenelemente.implementierte.Laenderkennzeichen
import net.dankito.fints.messages.datenelementgruppen.Datenelementgruppe


open class Kreditinstitutskennung(bankCountryCode: Int, bankCode: String)
    : Datenelementgruppe(listOf(
        Laenderkennzeichen(bankCountryCode, Existenzstatus.Mandatory),
        Kreditinstitutscode(bankCode, Existenzstatus.Mandatory)
    ), Existenzstatus.Mandatory)
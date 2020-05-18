package net.dankito.banking.fints.messages.datenelementgruppen.implementierte.tan

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Datum
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Uhrzeit
import net.dankito.banking.fints.messages.datenelementgruppen.Datenelementgruppe


/**
 * Datum und Uhrzeit, bis zu welchem Zeitpunkt eine TAN auf Basis der gesendeten Challenge
 * gültig ist. Nach Ablauf der Gültigkeitsdauer wird die entsprechende TAN entwertet.
 */
open class GueltigkeitsdatumUndUhrzeitFuerChallenge(date: Int, time: Int)
    : Datenelementgruppe(listOf(
        Datum(date, Existenzstatus.Mandatory),
        Uhrzeit(time, Existenzstatus.Mandatory)
), Existenzstatus.Optional)
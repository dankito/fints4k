package net.dankito.banking.fints.messages.datenelementgruppen.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Datum
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Uhrzeit
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.DatumUndZeitbezeichner
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.DatumUndZeitbezeichnerKodiert
import net.dankito.banking.fints.messages.datenelementgruppen.Datenelementgruppe


open class SicherheitsdatumUndUhrzeit(date: Int, time: Int)
    : Datenelementgruppe(listOf(
        DatumUndZeitbezeichnerKodiert(DatumUndZeitbezeichner.Sicherheitszeitstempel), // Als Bezeichner wird „1“ eingestellt, da es sich um einen Sicherheitszeitstempel handelt.
        Datum(date, Existenzstatus.Optional),
        Uhrzeit(time, Existenzstatus.Optional)
), Existenzstatus.Mandatory)
package net.dankito.banking.fints.messages.datenelemente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Information zur Referenzierung von Nachrichten innerhalb eines Dialoges. In
 * Zusammenhang mit der Dialog-ID und der Kundensystem-ID können Nachrichten
 * über die Nachrichtennummer auch dialogübergreifend eindeutig referenziert werden.
 * Eine Doppeleinreichungskontrolle ist mit Hilfe der Nachrichtennummer nicht möglich.
 *
 * Mit Hilfe der Nachrichtennummer nummerieren sowohl das Kundensystem als auch das
 * Kreditinstitutssystem seine Nachrichten unabhängig voneinander innerhalb eines Dialoges
 * in Einerschritten streng monoton aufsteigend. Die Nummerierung beginnt sowohl beim Kunden-
 * als auch beim Kreditinstitutssystem mit der Dialoginitialisierungsnachricht bei '1'.
 * Nachrichten, deren Nummerierung nicht streng monoton aufsteigend erfolgt ist, werden
 * institutsseitig bzw. kundenseitig abgelehnt.
 */
open class Nachrichtennummer(number: Int) : NumerischesDatenelement(number, 4, Existenzstatus.Mandatory) {

    companion object {
        const val FirstMessageNumber = 1
    }

}
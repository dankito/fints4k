package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Identifikation


/**
 * Eindeutig vergebene Kennung, anhand deren die Identifizierung des Benutzers erfolgt.
 * Die Vergabe obliegt dem Kreditinstitut. Das Kreditinstitut hat zu gewährleisten,
 * dass die Benutzerkennung institutsweit eindeutig ist. Sie kann beliebige Informationen
 * enthalten, darf aber bei Verwendung des RAH-Verfahrens aus Sicherheitsgründen nicht aus
 * benutzer- oder kreditinstitutsspezifischen Merkmalen hergeleitet werden.
 */
open class Benutzerkennung(identification: String) : Identifikation(identification, Existenzstatus.Mandatory)
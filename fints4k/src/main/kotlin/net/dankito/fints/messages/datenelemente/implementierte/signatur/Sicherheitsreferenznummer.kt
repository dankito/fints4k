package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Sicherheitsrelevante Nachrichtenidentifikation (Signatur-ID), welche zur Verhinderung der
 * Doppeleinreichung, respektive Garantie der Nachrichtensequenzintegrität eingesetzt werden kann.
 *
 * Bei chipkartenbasierten Verfahren ist der Sequenzzähler der Chipkarte einzustellen. Dies ist
 * bei Typ-1 Karten der Wert „EF_SEQ“ in der Application DF_BANKING und bei SECCOS Banken-
 * Signaturkarten der Wert „usage counter“ der beiden Signierschlüssel SK.CH.DS und SK.CH.AUT.
 *
 * Bei softwarebasierten Verfahren wird die Sicherheitsreferenznummer auf Basis des DE
 * Kundensystem-ID und des DE Benutzerkennung der DEG Schlüsselnamen verwaltet.
 */
open class Sicherheitsreferenznummer(number: Int) : NumerischesDatenelement(number, 16, Existenzstatus.Mandatory)
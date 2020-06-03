package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Identifikation der Funktion der beschriebenen Partei, in diesem Falle des Kunden.
 *
 * Codierung:
 * - 1: Message Sender (MS), wenn ein Kunde etwas an sein Kreditinstitut sendet.
 * - 2: Message Receiver (MR), wenn das Kreditinstitut etwas an seinen Kunden sendet.
 */
open class BezeichnerFuerSicherheitspartei : AlphanumerischesDatenelement("1", Existenzstatus.Mandatory, 3)
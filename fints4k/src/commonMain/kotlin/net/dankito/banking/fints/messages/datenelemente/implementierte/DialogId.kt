package net.dankito.banking.fints.messages.datenelemente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Identifikation


/**
 * Die Dialog-ID dient der eindeutigen Zuordnung einer Nachricht zu einem FinTS-Dialog.
 * Die erste Kundennachricht (Dialoginitialisierung) enthält als Dialog-ID den Wert 0.
 * In der ersten Antwortnachricht wird vom Kreditinstitut eine Dialog-ID vorgegeben,
 * die für alle nachfolgenden Nachrichten dieses Dialogs einzustellen ist. Es ist
 * Aufgabe des Kreditinstituts, dafür zu sorgen, dass diese Dialog-ID
 * dialogübergreifend und systemweit eindeutig ist.
 */
open class DialogId(dialogId: String) : Identifikation(dialogId, Existenzstatus.Mandatory)
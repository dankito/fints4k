package net.dankito.fints.messages.datenelemente.implementierte.tan

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Enthält im Falle des Zwei-Schritt-TAN-Verfahrens die Referenz auf einen eingereichten Auftrag.
 * Die Auftragsreferenz wird bei der späteren Einreichung der zugehörigen TANs (mittels HKTAN bei
 * TAN-Prozess=2 bzw. 3) zur Referenzierung des Auftrags verwendet.
 *
 * Da die Auftragsreferenz immer eindeutig ist, sollten Kundenprodukte diese als zentrale
 * Referenzierung verwenden und dem Kunden auch zusammen mit den Auftragsdaten präsentieren bzw.
 * für die Problemverfolgung leicht zugänglich machen.
 */
open class Auftragsreferenz(reference: String, existenzstatus: Existenzstatus)
    : AlphanumerischesDatenelement(reference, existenzstatus, 35)
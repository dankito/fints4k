package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Name des Kundenproduktes, mit dem kundenseitig die Nachrichten erzeugt wurden.
 * Diese Angabe dient dem Kreditinstitut, um Kundenprodukthersteller gezielt unterstützen zu können.
 *
 * Die Produktbezeichnung ist verpflichtend mit aussagekräftigen Informationen über das verwendete
 * Kundenprodukt, nicht eine ggf. verwendete interne FinTS-/HBCI-Bibliothek, zu füllen, um
 * Support-Anfragen leichter beantworten zu können.
 *
 * Kundenprodukte, die nach dem durch die Deutsche Kreditwirtschaft festgelegten Verfahren registriert
 * sind, müssen in dieses DE die vergebene Produktregistrierungsnummer einstellen.
 */
open class Produktbezeichnung(name: String, existenzstatus: Existenzstatus)
    : AlphanumerischesDatenelement(name, existenzstatus, 25)
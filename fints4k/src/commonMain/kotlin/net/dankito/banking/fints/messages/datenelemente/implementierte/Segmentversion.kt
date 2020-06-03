package net.dankito.banking.fints.messages.datenelemente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Versionsnummer zur Dokumentation von Änderungen eines Segmentformats.
 * Die Segmentversion von administrativen Segmenten (die Segmentart ‘Administration’ bzw. ‘Geschäftsvorfall’
 * ist bei jeder Segmentbeschreibung angegeben) wird bei jeder Änderung des Segmentformats inkrementiert.
 *
 * Bei Geschäftsvorfallssegmenten wird die Segmentversion auf logischer Ebene verwaltet, d. h. sie ist für das
 * Auftrags-, das Antwort- und das Parametersegment des Geschäftsvorfalls stets identisch und wird inkrementiert,
 * wenn sich das Format von mindestens einem der drei Segmente ändert.
 *
 * Dieses Verfahren gilt bei Standardsegmenten einheitlich für alle Kreditinstitute. Bei verbandsindividuellen
 * Segmenten obliegt die Versionssteuerung dem jeweiligen Verband. Der Zeitpunkt der Unterstützung einer neuen
 * Segmentversion kann jedoch zwischen den Verbänden variieren.
 *
 * Die für die jeweilige HBCI-Version gültige Segmentversion ist bei der jeweiligen Segmentbeschreibung vermerkt.
 *
 * Falls der Kunde ein Segment mit einer veralteten Versionsnummer einreicht, sollte ihm in einer entsprechenden
 * Warnung rückgemeldet werden, dass sein Kundenprodukt aktualisiert werden sollte.
 */
open class Segmentversion(version: Int) : NumerischesDatenelement(version, 3, Existenzstatus.Mandatory)
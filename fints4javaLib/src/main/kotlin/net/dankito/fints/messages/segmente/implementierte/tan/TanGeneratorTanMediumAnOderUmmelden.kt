package net.dankito.fints.messages.segmente.implementierte.tan

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Datum
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement
import net.dankito.fints.messages.datenelemente.basisformate.NumerischesDatenelement
import net.dankito.fints.messages.datenelemente.implementierte.DoNotPrintDatenelement
import net.dankito.fints.messages.datenelemente.implementierte.NotAllowedDatenelement
import net.dankito.fints.messages.datenelemente.implementierte.allCodes
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanMediumKlasse
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.datenelementgruppen.implementierte.account.Kontoverbindung
import net.dankito.fints.messages.datenelementgruppen.implementierte.account.KontoverbindungInternational
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.fints.model.BankData
import net.dankito.fints.model.CustomerData
import net.dankito.fints.response.segments.ChangeTanMediaParameters


/**
 * The actual job is called "TAN-Medium an- bzw. ummelden", but as TAN lists aren't supported anymore I've implemented
 * it only for TAN Generators.
 *
 * Mit Hilfe dieses Geschäftsvorfalls kann der Kunde seinem Institut mitteilen, welches Medium (Chipkarte,
 * TAN-Generator oder bilateral vereinbart) er für die Autorisierung der Aufträge per TAN verwenden wird.
 *
 * Welches Medium gerade aktiv ist, kann mit Hilfe des Geschäftsvorfalls „TAN-Medium anzeigen Bestand (HKTAB)“ bzw.
 * für Detailinformationen zur Karte auch „Kartenanzeige anfordern (HKAZK)“ durch den Kunden erfragt werden.
 *
 * Der Kunde entscheidet selbst, welches seiner verfügbaren TAN-Medien er verwenden möchte.
 *
 * chipTAN-Verfahren:
 * Steht beim chipTAN-Verfahren ein Kartenwechsel an, so kann der Kunde mit diesem Geschäftsvorfall seine Karte bzw.
 * Folgekarte aktivieren. Kann der Kunde mehrere Karten verwenden, dann kann mit diesem GV die Ummeldung auf eine
 * andere Karte erfolgen. Das Kreditinstitut entscheidet selbst, ob dieser GV TAN-pflichtig istoder nicht.
 */
open class TanGeneratorTanMediumAnOderUmmelden(
    segmentVersion: Int,
    segmentNumber: Int,
    bank: BankData,
    customer: CustomerData,
    newActiveTanMedium: TanGeneratorTanMedium,
    /**
     * Has to be set if „Eingabe von ATC und TAN erforderlich“ (BPD)=“J“
     */
    tan: String? = null,
    /**
     * Has to be set if „Eingabe von ATC und TAN erforderlich“ (BPD)=“J“
     */
    atc: Int? = null,
    /**
     * An optional field and only used in version 3
     */
    iccsn: String? = null,

    parameters: ChangeTanMediaParameters = bank.changeTanMediumParameters!!
)
    : Segment(listOf(
    Segmentkopf(CustomerSegmentId.ChangeTanMedium, segmentVersion, segmentNumber),
    Code(TanMediumKlasse.TanGenerator, allCodes<TanMediumKlasse>(), Existenzstatus.Mandatory),
    AlphanumerischesDatenelement(newActiveTanMedium.cardNumber, Existenzstatus.Mandatory),
    AlphanumerischesDatenelement(newActiveTanMedium.cardSequenceNumber, if (parameters.enteringCardSequenceNumberRequired) Existenzstatus.Mandatory else Existenzstatus.NotAllowed),
    if (segmentVersion > 1) NumerischesDatenelement(newActiveTanMedium.cardType, 2, if (parameters.enteringCardTypeAllowed) Existenzstatus.Optional else Existenzstatus.NotAllowed) else DoNotPrintDatenelement(),
    if (segmentVersion == 2) Kontoverbindung(bank, customer, customer.accounts.firstOrNull()) else DoNotPrintDatenelement(),
    if (segmentVersion >= 3 && parameters.accountInfoRequired) KontoverbindungInternational(bank, customer, customer.accounts.firstOrNull()) else DoNotPrintDatenelement(),
    if (segmentVersion >= 2) Datum(newActiveTanMedium.validFrom, Existenzstatus.Optional) else DoNotPrintDatenelement(),
    if (segmentVersion >= 2) Datum(newActiveTanMedium.validTo, Existenzstatus.Optional) else DoNotPrintDatenelement(),
    if (segmentVersion >= 3) AlphanumerischesDatenelement(iccsn, Existenzstatus.Optional, 19) else DoNotPrintDatenelement(),
    NotAllowedDatenelement(), // TAN-Listennummer not supported anymore
    NumerischesDatenelement(atc, 5, if (parameters.enteringAtcAndTanRequired) Existenzstatus.Mandatory else Existenzstatus.NotAllowed),
    AlphanumerischesDatenelement(tan, if (parameters.enteringAtcAndTanRequired) Existenzstatus.Mandatory else Existenzstatus.NotAllowed, 99)
)) {

    init {
        if (parameters.enteringAtcAndTanRequired) {
            if (atc == null || tan == null) {
                throw UnsupportedOperationException("As „Eingabe von ATC und TAN erforderlich“ is set to \"J\" " +
                        "(ChangeTanMediaParameters.enteringAtcAndTanRequired is set to true) parameters atc and tan have to be set.")
            }
        }
    }

}
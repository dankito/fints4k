package net.dankito.banking.fints.response.segments


/**
 * TODO: some translations:
 *
 * - payee: Zahlungsempfänger
 * - note to payee: Verwendungszweck
 * - alternative payee: abweichender Zahlungsempfänger
 */

open class SepaAccountInfoParameters(
    parameters: JobParameters,

    /**
     * Über das DE „Einzelkontoabruf erlaubt“ legt das Kreditinstitut fest, ob es möglich ist, einzelne
     * Kontoverbindungen gezielt abzurufen oder ob nur alle relevanten Konten insgesamt bereitgestellt werden.
     */
    val retrieveSingleAccountAllowed: Boolean,

    /**
     * Über das DE „Nationale Kontoverbindung erlaubt“ legt das Kreditinstitut fest, ob im Rahmen einer
     * SEPA-Kontoverbindung auch die nationalen Elemente Kreditinstitutskennung, Konto-/Depotnummer und
     * Unterkontomerkmal zugelassen sind. Bei „N“ dürfen nur IBAN und BIC verwendet werden.
     */
    val nationalAccountRelationshipAllowed: Boolean,

    /**
     * Über diese Information legt das Kreditinstitut fest, ob bei SEPA-Zahlungsverkehrsinstrumenten die Verwendung
     * von strukturierten Verwendungszweckinformationen („StructuredRemittanceInformation“) erlaubt ist oder nicht.
     */
    val structuredUsageAllowed: Boolean,

    /**
     * Kennzeichen dafür, ob die Belegung des Feldes „Maximale Anzahl Einträge“ im Kundenauftrag zugelassen ist.
     * Falls ja, kann das Kundenprodukt die Anzahl der maximal rückzumeldenden Buchungspositionen beschränken.
     *
     * Über das DE „Eingabe Anzahl Einträge erlaubt“ legt das Kreditinstitut fest, ob es kundenseitig möglich ist,
     * bei Aufträgen die Anzahl von Einträgen in derKreditinstitutsantwort zu beschränken. Ist die Option nicht
     * zugelassen, gelten die syntaktischen Maximalwerte.
     */
    val settingMaxAllowedEntriesAllowed: Boolean,

    /**
     * Anzahl der Stellen im SEPA Verwendungszweck (CreditorReferenceInformationSCT, insgesamt 4 x 35 = 140 Stellen),
     * die für interne Verwendung – z. B. Andrucken von Datum, Uhrzeit und verwendeter TAN – durch das Institut
     * reserviert sind. Diese Stellen dürfen vom Kundenprodukt nicht für andere Zwecke verwendet werden. Die Anzahl
     * wird vom Ende des letzten SEPA-Elementes aus gezählt und darf den Wert 35 nicht überschreiten.
     */
    val countReservedUsageLength: Int,

    /**
     * Dieses DE beschreibt Ort, Name und Version einer SEPA pain message als URN. Die korrekte Bezeichnung des URN
     * ist der Anlage 3 des DFÜ-Abkommens zu entnehmen (vgl. [DFÜ-Abkommen]).
     *
     * Für die pain messages der ersten Generation ("pain.00x.001.0y.xsd") sind weiterhin die bisherigen Regelungen
     * (Angabe der URI bzw. "sepade.pain.00x.001.0y.xsd") zugelassen. Bestehende, lauffähige Implementierungen für
     * diese erste Schema-Generation müssen somit nicht angepasst werden.
     *
     * Werden in den Bankparameterdaten eines bestimmten Geschäftsvorfalls explizit unterstützte SEPA-Datenformate
     * genannt, so sind die laut HISPAS global mitgeteilten unterstützten SEPA pain messages für den betreffenden
     * Geschäftsvorfall nicht relevant. Es gelten lediglich die laut den Bankparameterdaten des Geschäftsvorfalls
     * zugelassenen SEPA pain messages.
     */
    val supportedSepaFormats: List<String>
)
    : JobParameters(parameters) {

    companion object {
        const val CountReservedUsageLengthNotSet = 0
    }

}
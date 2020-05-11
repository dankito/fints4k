package net.dankito.fints.response.segments

import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.HbciVersion


open class BankParameters(
    val bpdVersion: Int,

    val bankCountryCode: Int,

    val bankCode: String,

    val bankName: String,

    /**
     * Maximale Anzahl an Geschäftsvorfallsarten, die pro Nachricht zulässig ist.
     * Der Wert ‚0’ gibt an, dass keine Restriktionen bzgl. der Anzahl an Geschäftsvorfallsarten bestehen.
     */
    val countMaxJobsPerMessage: Int,

    val supportedLanguages: List<Dialogsprache>,

    val supportedHbciVersions: List<HbciVersion>,

    /**
     * Obergrenze in Kilobyte (=1024 Byte) für die Nachrichtengröße. Dies kann kreditinstitutsindividuell
     * je nach technischen Restriktionen bzgl. der Verarbeitung umfangreicher Kundennachrichten vorgegeben werden.
     *
     * Der Wert ‚0’ gibt an, dass keine Restriktionen bzgl. der Nachrichtengröße bestehen.
     *
     * Eingehende Nachrichten, die dekomprimiert und entschlüsselt diese Grenze überschreiten, können dann
     * abgelehnt werden.
     */
    val maxMessageSize: Int?,

    /**
     * Zeitraum, nach dem frühestens eine weitere Life-Indikator-Nachricht gesendet werden darf.
     * Die Angabe erfolgt in Sekunden. Liegt keine Begrenzung vor, kann der Wert ‚0’ angegeben werden.
     */
    val minTimeout: Int?,

    /**
     * Zeitraum, nach dem das Kreditinstitut einen Dialog voraussichtlich beenden wird, sofern keine
     * weiteren Kundennachrichten gesendet wurden. Die Angabe erfolgt in Sekunden. Liegt keine
     * Begrenzung vor, kann der Wert ‚0’ angegeben werden.
     */
    val maxTimeout: Int?,

    segmentString: String

) : ReceivedSegment(segmentString)
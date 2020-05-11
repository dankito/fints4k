package net.dankito.fints.response.segments

import net.dankito.fints.messages.datenelementgruppen.implementierte.signatur.Sicherheitsprofil


open class SecurityMethods(

    /**
     * Kennzeichen dafür, ob das Kreditinstitut die Mischung von Sicherheitsverfahren zulässt, sofern es mehrere
     * Sicherheitsverfahren anbietet. Hierunter ist zu verstehen,
     * - dass eine Nachricht von mehreren Benutzern mit unterschiedlichen Verfahren signiert wird.
     * - dass ein Benutzer die Nachrichten eines Dialoges mit verschiedenen Verfahren signiert.
     * - dass Signatur und Verschlüsselung einer Nachricht mit verschiedenen Verfahren durchgeführt werden.
     * - dass zwischen den folgenden Gruppen gemischt werden soll:
     *      + RAH-7, RAH-9, RDH-3, RDH-5, RDH-6, RDH-7, RDH-8 und RDH-9
     *      + RAH-10, RDH-2 und RDH-10
     *      + DDV
     *      + PIN
     *
     * Eine Verwendung von Sicherheitsverfahren innerhalb dieser Gruppen gilt nicht als Mischung.
     *
     * Ist hier ‘N’ eingestellt, so sind die genannten Fälle nicht zulässig, d. h. alle Signaturen und
     * Verschlüsselungen eines Dialoges müssen mit demselben Sicherheitsverfahren bzw. mit Verfahren aus der gleichen
     * Gruppe vorgenommen werden. Ist ‘J’ eingestellt, so müssen kreditinstitutsseitig alle vorgenannten Fälle unterstützt werden.
     *
     * Falls das Kreditinstitut nur ein Sicherheitsverfahren anbietet, ist ‘N’ einzustellen.
     */
    val mixingAllowed: Boolean,

    val securityProfiles: List<Sicherheitsprofil>,

    segmentString: String
)
    : ReceivedSegment(segmentString)
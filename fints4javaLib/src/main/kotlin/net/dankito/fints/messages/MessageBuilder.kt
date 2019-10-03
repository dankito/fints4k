package net.dankito.fints.messages

import net.dankito.fints.messages.datenelemente.implementierte.*
import net.dankito.fints.messages.nachrichten.Nachricht
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.implementierte.IdentifikationsSegment
import net.dankito.fints.messages.segmente.implementierte.Nachrichtenabschluss
import net.dankito.fints.messages.segmente.implementierte.Nachrichtenkopf
import net.dankito.fints.messages.segmente.implementierte.Verarbeitungsvorbereitung


/**
 * Takes the Segments of they payload, may signs and encrypts them, calculates message size,
 * adds the message header and closing, and formats the whole message to string.
 */
open class MessageBuilder {

    companion object {
        const val MessageHeaderLength = 30
        const val MessageClosingLength = 11
        const val AddedSeparatorsLength = 3
    }


    /**
     * Um Kunden die Möglichkeit zu geben, sich anonym anzumelden, um sich bspw. über die
     * angebotenen Geschäftsvorfälle fremder Kreditinstitute (von denen sie keine BPD besitzen)
     * zu informieren bzw. nicht-signierungspflichtige Aufträge bei fremden Kreditinstituten
     * einreichen zu können, kann sich der Kunde anonym (als Gast) anmelden.
     *
     * Bei anonymen Dialogen werden Nachrichten weder signiert, noch können sie verschlüsselt und komprimiert werden.
     */
    open fun createAnonymousDialogInitMessage(
        bankCountryCode: Int,
        bankCode: String,
        productName: String,
        productVersion: String
    ): String {
        return createDialogInitMessage(bankCountryCode, bankCode, KundenID.Anonymous, KundensystemID.Anonymous,
            BPDVersion.VersionNotReceivedYet, UPDVersion.VersionNotReceivedYet, Dialogsprache.Default, productName, productVersion)
    }

    open fun createDialogInitMessage(
        bankCountryCode: Int,
        bankCode: String,
        customerId: String,
        customerSystemId: String,
        bpdVersion: Int,
        updVersion: Int,
        language: Dialogsprache,
        productName: String,
        productVersion: String
    ): String {
        return createMessage(listOf(
            IdentifikationsSegment(2, bankCountryCode, bankCode, customerId, customerSystemId),
            Verarbeitungsvorbereitung(3, bpdVersion, updVersion, language, productName, productVersion)
        ))
    }


    open fun createMessage(payloadSegments: List<Segment>): String {

        val payload = payloadSegments.joinToString(Nachricht.SegmentSeparator) { it.format() }

        val messageSize = payload.length + MessageHeaderLength + MessageClosingLength + AddedSeparatorsLength
        val messageNumber = Nachrichtennummer.FirstMessageNumber

        val header = Nachrichtenkopf(1, messageSize, "0", messageNumber)

        val closing = Nachrichtenabschluss(4, messageNumber)

        return listOf(header.format(), payload, closing.format())
            .joinToString(Nachricht.SegmentSeparator, postfix = Nachricht.SegmentSeparator)
    }

}
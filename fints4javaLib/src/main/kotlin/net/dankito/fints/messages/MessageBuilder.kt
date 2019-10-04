package net.dankito.fints.messages

import net.dankito.fints.messages.datenelemente.implementierte.*
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.fints.messages.nachrichten.Nachricht
import net.dankito.fints.messages.segmente.ISegmentNumberGenerator
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.SegmentNumberGenerator
import net.dankito.fints.messages.segmente.implementierte.*
import net.dankito.fints.util.FinTsUtils


/**
 * Takes the Segments of they payload, may signs and encrypts them, calculates message size,
 * adds the message header and closing, and formats the whole message to string.
 */
open class MessageBuilder(protected val generator: ISegmentNumberGenerator = SegmentNumberGenerator(),
                          protected val utils: FinTsUtils = FinTsUtils()) {

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

        return createDialogInitMessage(bankCountryCode, bankCode, KundenID.Anonymous, KundensystemID.Anonymous, KundensystemStatusWerte.NichtBenoetigt,
            BPDVersion.VersionNotReceivedYet, UPDVersion.VersionNotReceivedYet, Dialogsprache.Default, productName, productVersion, false)
    }

    open fun createDialogInitMessage(
        bankCountryCode: Int,
        bankCode: String,
        customerId: String,
        customerSystemId: String,
        status: KundensystemStatusWerte,
        bpdVersion: Int,
        updVersion: Int,
        language: Dialogsprache,
        productName: String,
        productVersion: String,
        signMessage: Boolean = true
    ): String {

        return createMessage(signMessage, bankCountryCode, bankCode, customerId, listOf(
            IdentifikationsSegment(generator.resetSegmentNumber(if (signMessage) 2 else 1), bankCountryCode, bankCode, customerId, customerSystemId, status),
            Verarbeitungsvorbereitung(generator.getNextSegmentNumber(), bpdVersion, updVersion, language, productName, productVersion)
        ))
    }


    open fun createMessage(signMessage: Boolean, bankCountryCode: Int, bankCode: String, customerId: String,
                           payloadSegments: List<Segment>): String {

        val signedPayload = if (signMessage) { signPayload(2, bankCountryCode, bankCode, customerId, payloadSegments) }
                            else { payloadSegments }

        val payload = signedPayload.joinToString(Nachricht.SegmentSeparator) { it.format() }

        val messageSize = payload.length + MessageHeaderLength + MessageClosingLength + AddedSeparatorsLength
        val messageNumber = Nachrichtennummer.FirstMessageNumber

        val header = Nachrichtenkopf(ISegmentNumberGenerator.FirstSegmentNumber, messageSize, "0", messageNumber)

        val closing = Nachrichtenabschluss(generator.getNextSegmentNumber(), messageNumber)

        return listOf(header.format(), payload, closing.format())
            .joinToString(Nachricht.SegmentSeparator, postfix = Nachricht.SegmentSeparator)
    }

    protected open fun signPayload(headerSegmentNumber: Int, bankCountryCode: Int, bankCode: String, customerId: String,
                                   payloadSegments: List<Segment>): List<Segment> {
        val controlReference = "1" // TODO

        val signatureHeader = PinTanSignaturkopf(
            headerSegmentNumber,
            Sicherheitsfunktion.PIN_TAN_911, // TODO
            controlReference,
            "0",
            utils.formatDateTodayAsInt(),
            utils.formatTimeNowAsInt(),
            bankCountryCode,
            bankCode,
            customerId
        )

        val signatureClosing = Signaturabschluss(
            generator.getNextSegmentNumber(),
            controlReference,
            "" // TODO
        )

        return listOf(signatureHeader, *payloadSegments.toTypedArray(), signatureClosing)
    }

}
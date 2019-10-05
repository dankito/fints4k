package net.dankito.fints.messages

import net.dankito.fints.messages.datenelemente.implementierte.tan.TanProcess
import net.dankito.fints.messages.nachrichten.Nachricht
import net.dankito.fints.messages.segmente.ISegmentNumberGenerator
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.SegmentId
import net.dankito.fints.messages.segmente.SegmentNumberGenerator
import net.dankito.fints.messages.segmente.implementierte.*
import net.dankito.fints.model.BankData
import net.dankito.fints.model.CustomerData
import net.dankito.fints.model.DialogData
import net.dankito.fints.model.ProductData
import net.dankito.fints.util.FinTsUtils
import java.util.concurrent.ThreadLocalRandom


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
    open fun createAnonymousDialogInitMessage(bank: BankData, product: ProductData, dialogData: DialogData): String {

        val customer = CustomerData.Anonymous

        return createMessage(false, false, bank, customer, dialogData, listOf(
            IdentifikationsSegment(generator.resetSegmentNumber(1), bank, customer),
            Verarbeitungsvorbereitung(generator.getNextSegmentNumber(), bank, customer, product)
        ))
    }

    open fun createAnonymousDialogEndMessage(bank: BankData, dialogData: DialogData): String {

        val customer = CustomerData.Anonymous

        return createMessage(false, false, bank, customer, dialogData, listOf(
            Dialogende(generator.resetSegmentNumber(1), dialogData)
        ))
    }


    open fun createDialogInitMessage(bank: BankData, customer: CustomerData, product: ProductData, dialogData: DialogData): String {

        return createMessage(true, true, bank, customer, dialogData, listOf(
            IdentifikationsSegment(generator.resetSegmentNumber(2), bank, customer),
            Verarbeitungsvorbereitung(generator.getNextSegmentNumber(), bank, customer, product),
            ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, SegmentId.Identification.id)
        ))
    }

    open fun createDialogEndMessage(bank: BankData, customer: CustomerData, dialogData: DialogData): String {

        return createMessage(true, true, bank, customer, dialogData, listOf(
            Dialogende(generator.resetSegmentNumber(2), dialogData)
        ))
    }


    open fun createMessage(signMessage: Boolean, encryptMessage: Boolean, bank: BankData, customer: CustomerData,
                           dialogData: DialogData, payloadSegments: List<Segment>): String {

        var payload = payloadSegments
        val date = utils.formatDateTodayAsInt()
        val time = utils.formatTimeNowAsInt()

        if (signMessage) {
            payload = signPayload(2, bank, customer, date, time, payload)
        }

        if (encryptMessage) {
            payload = encryptPayload(bank, customer, date, time, payload)
        }

        val formattedPayload = formatPayload(payload)

        val messageSize = formattedPayload.length + MessageHeaderLength + MessageClosingLength + AddedSeparatorsLength

        val header = Nachrichtenkopf(ISegmentNumberGenerator.FirstSegmentNumber, messageSize, dialogData)

        val closing = Nachrichtenabschluss(generator.getNextSegmentNumber(), dialogData)

        return listOf(header.format(), formattedPayload, closing.format())
            .joinToString(Nachricht.SegmentSeparator, postfix = Nachricht.SegmentSeparator)
    }


    protected open fun signPayload(headerSegmentNumber: Int, bank: BankData, customer: CustomerData, date: Int, time: Int,
                                   payloadSegments: List<Segment>): List<Segment> {
        val controlReference = Math.abs(ThreadLocalRandom.current().nextInt()).toString()

        val signatureHeader = PinTanSignaturkopf(
            headerSegmentNumber,
            bank,
            customer,
            controlReference,
            date,
            time
        )

        val signatureClosing = Signaturabschluss(
            generator.getNextSegmentNumber(),
            controlReference,
            customer.pin
        )

        return listOf(signatureHeader, *payloadSegments.toTypedArray(), signatureClosing)
    }


    private fun encryptPayload(bank: BankData, customer: CustomerData, date: Int, time: Int,
                               payload: List<Segment>): List<Segment> {

        val encryptionHeader = PinTanVerschluesselungskopf(bank, customer, date, time)

        val encryptedData = VerschluesselteDaten(formatPayload(payload) + Nachricht.SegmentSeparator)

        return listOf(encryptionHeader, encryptedData)
    }


    protected open fun formatPayload(payload: List<Segment>): String {
        return payload.joinToString(Nachricht.SegmentSeparator) { it.format() }
    }

}
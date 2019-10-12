package net.dankito.fints.messages

import net.dankito.fints.messages.datenelemente.implementierte.Synchronisierungsmodus
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanProcess
import net.dankito.fints.messages.segmente.ISegmentNumberGenerator
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.SegmentNumberGenerator
import net.dankito.fints.messages.segmente.Synchronisierung
import net.dankito.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.fints.messages.segmente.implementierte.*
import net.dankito.fints.messages.segmente.implementierte.sepa.SepaEinzelueberweisung
import net.dankito.fints.messages.segmente.implementierte.umsaetze.KontoumsaetzeZeitraumMt940Version5
import net.dankito.fints.messages.segmente.implementierte.umsaetze.Saldenabfrage
import net.dankito.fints.model.*
import net.dankito.fints.util.FinTsUtils
import java.util.concurrent.ThreadLocalRandom


/**
 * Takes the Segments of they payload, may signs and encrypts them, calculates message size,
 * adds the message header and ending, and formats the whole message to string.
 */
open class MessageBuilder(protected val generator: ISegmentNumberGenerator = SegmentNumberGenerator(),
                          protected val utils: FinTsUtils = FinTsUtils()) {

    companion object {
        const val MessageHeaderLength = 30
        const val MessageEndingLength = 11
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

        return createMessage(bank, customer, dialogData, listOf(
            IdentifikationsSegment(generator.resetSegmentNumber(1), bank, customer),
            Verarbeitungsvorbereitung(generator.getNextSegmentNumber(), bank, customer, product)
        ))
    }

    open fun createAnonymousDialogEndMessage(bank: BankData, dialogData: DialogData): String {

        val customer = CustomerData.Anonymous

        return createMessage(bank, customer, dialogData, listOf(
            Dialogende(generator.resetSegmentNumber(1), dialogData)
        ))
    }


    open fun createInitDialogMessage(bank: BankData, customer: CustomerData, product: ProductData, dialogData: DialogData): String {

        return createSignedMessage(bank, customer, dialogData, listOf(
            IdentifikationsSegment(generator.resetSegmentNumber(2), bank, customer),
            Verarbeitungsvorbereitung(generator.getNextSegmentNumber(), bank, customer, product),
            ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, CustomerSegmentId.Identification)
        ))
    }

    open fun createSynchronizeCustomerSystemIdMessage(bank: BankData, customer: CustomerData, product: ProductData, dialogData: DialogData): String {

        return createSignedMessage(bank, customer, dialogData, listOf(
            IdentifikationsSegment(generator.resetSegmentNumber(2), bank, customer),
            Verarbeitungsvorbereitung(generator.getNextSegmentNumber(), bank, customer, product),
            ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, CustomerSegmentId.Identification),
            Synchronisierung(generator.getNextSegmentNumber(), Synchronisierungsmodus.NeueKundensystemIdZurueckmelden)
        ))
    }

    open fun createDialogEndMessage(bank: BankData, customer: CustomerData, dialogData: DialogData): String {

        return createSignedMessage(bank, customer, dialogData, listOf(
            Dialogende(generator.resetSegmentNumber(2), dialogData)
        ))
    }


    open fun createGetTransactionsMessage(parameter: GetTransactionsParameter, bank: BankData, customer: CustomerData,
                                          product: ProductData, dialogData: DialogData): String {

        return createSignedMessage(bank, customer, dialogData, listOf(
            KontoumsaetzeZeitraumMt940Version5(generator.resetSegmentNumber(2), parameter, bank, customer),
            ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, CustomerSegmentId.AccountTransactionsMt940)
        ))
    }

    open fun createGetBalanceMessage(bank: BankData, customer: CustomerData, product: ProductData, dialogData: DialogData): String {

        return createSignedMessage(bank, customer, dialogData, listOf(
            Saldenabfrage(
                generator.resetSegmentNumber(2),
                bank,
                customer,
                false
            ),
            ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, CustomerSegmentId.Balance)
        ))
    }


    open fun createBankTransferMessage(bankTransferData: BankTransferData, bank: BankData, customer: CustomerData, dialogData: DialogData): String {

        return createSignedMessage(bank, customer, dialogData, listOf(
            SepaEinzelueberweisung(generator.resetSegmentNumber(2), customer, bank.bic!!, bankTransferData),
            ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, CustomerSegmentId.SepaBankTransfer)
        ))
    }


    open fun createSignedMessage(bank: BankData, customer: CustomerData, dialogData: DialogData,
                                 payloadSegments: List<Segment>): String {

        val date = utils.formatDateTodayAsInt()
        val time = utils.formatTimeNowAsInt()

        val signedPayload = signPayload(2, bank, customer, date, time, payloadSegments)

        val encryptedPayload = encryptPayload(bank, customer, date, time, signedPayload)

        return createMessage(bank, customer, dialogData, encryptedPayload)
    }

    open fun createMessage(bank: BankData, customer: CustomerData, dialogData: DialogData,
                           payloadSegments: List<Segment>): String {

        val formattedPayload = formatPayload(payloadSegments)

        val messageSize = formattedPayload.length + MessageHeaderLength + MessageEndingLength + AddedSeparatorsLength

        val header = Nachrichtenkopf(ISegmentNumberGenerator.FirstSegmentNumber, messageSize, dialogData)

        val ending = Nachrichtenabschluss(generator.getNextSegmentNumber(), dialogData)

        return listOf(header.format(), formattedPayload, ending.format())
            .joinToString(Separators.SegmentSeparator, postfix = Separators.SegmentSeparator)
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

        val signatureEnding = Signaturabschluss(
            generator.getNextSegmentNumber(),
            controlReference,
            customer.pin
        )

        return listOf(signatureHeader, *payloadSegments.toTypedArray(), signatureEnding)
    }


    private fun encryptPayload(bank: BankData, customer: CustomerData, date: Int, time: Int,
                               payload: List<Segment>): List<Segment> {

        val encryptionHeader = PinTanVerschluesselungskopf(bank, customer, date, time)

        val encryptedData = VerschluesselteDaten(formatPayload(payload) + Separators.SegmentSeparator)

        return listOf(encryptionHeader, encryptedData)
    }


    protected open fun formatPayload(payload: List<Segment>): String {
        return payload.joinToString(Separators.SegmentSeparator) { it.format() }
    }

}
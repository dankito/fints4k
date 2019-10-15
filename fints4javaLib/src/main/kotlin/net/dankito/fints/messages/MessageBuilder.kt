package net.dankito.fints.messages

import net.dankito.fints.extensions.containsAny
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
import net.dankito.fints.messages.segmente.implementierte.umsaetze.KontoumsaetzeZeitraumMt940Version6
import net.dankito.fints.messages.segmente.implementierte.umsaetze.KontoumsaetzeZeitraumMt940Version7
import net.dankito.fints.messages.segmente.implementierte.umsaetze.Saldenabfrage
import net.dankito.fints.model.*
import net.dankito.fints.util.FinTsUtils
import kotlin.random.Random


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


    open fun createInitDialogMessage(bank: BankData, customer: CustomerData, product: ProductData,
                                     dialogData: DialogData, useStrongAuthentication: Boolean = true): String {

        val segments = mutableListOf(
            IdentifikationsSegment(generator.resetSegmentNumber(2), bank, customer),
            Verarbeitungsvorbereitung(generator.getNextSegmentNumber(), bank, customer, product)
        )

        if (useStrongAuthentication) {
            segments.add(ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, CustomerSegmentId.Identification))
        }

        return createSignedMessage(bank, customer, dialogData, segments)
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
                                          product: ProductData, dialogData: DialogData): MessageBuilderResult {

        val result = getSupportedVersionOfJob(CustomerSegmentId.AccountTransactionsMt940, customer, listOf(5, 6, 7))

        if (result.isJobVersionSupported) {
            val transactionsJob = if (result.isAllowed(7)) KontoumsaetzeZeitraumMt940Version7(generator.resetSegmentNumber(2), parameter, bank, customer)
            else if (result.isAllowed(6)) KontoumsaetzeZeitraumMt940Version6(generator.resetSegmentNumber(2), parameter, bank, customer)
            else KontoumsaetzeZeitraumMt940Version5(generator.resetSegmentNumber(2), parameter, bank, customer)


            return MessageBuilderResult(createSignedMessage(bank, customer, dialogData, listOf(
                transactionsJob,
                ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, CustomerSegmentId.AccountTransactionsMt940)
            )))
        }

        return result
    }

    open fun createGetBalanceMessage(bank: BankData, customer: CustomerData, product: ProductData, dialogData: DialogData): MessageBuilderResult {

        val result = getSupportedVersionOfJob(CustomerSegmentId.Balance, customer, listOf(5))

        if (result.isJobVersionSupported) {
            return MessageBuilderResult(createSignedMessage(bank, customer, dialogData, listOf(
                Saldenabfrage(generator.resetSegmentNumber(2), bank, customer),
                ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, CustomerSegmentId.Balance)
            )))
        }

        return result
    }


    open fun createBankTransferMessage(bankTransferData: BankTransferData, bank: BankData, customer: CustomerData, dialogData: DialogData): MessageBuilderResult {

        val result = getSupportedVersionOfJob(CustomerSegmentId.SepaBankTransfer, customer, listOf(1))

        if (result.isJobVersionSupported) {

            return MessageBuilderResult(createSignedMessage(bank, customer, dialogData, listOf(
                SepaEinzelueberweisung(generator.resetSegmentNumber(2), customer, bank.bic!!, bankTransferData),
                ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, CustomerSegmentId.SepaBankTransfer)
            )))
        }

        return result
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
        val controlReference = createControlReference()

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

    protected open fun createControlReference(): String {
        return Math.abs(Random(System.nanoTime()).nextInt()).toString()
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


    protected open fun getSupportedVersionOfJob(segmentId: CustomerSegmentId, customer: CustomerData,
                                                supportedVersions: List<Int>): MessageBuilderResult {

        customer.accounts.firstOrNull()?.let { account -> // TODO: find a better solution / make more generic
            val allowedVersions = account.allowedJobs.filter { it.jobName == segmentId.id }
                .map { it.segmentVersion }
                .sortedDescending()

            return MessageBuilderResult(allowedVersions.isNotEmpty(), allowedVersions.containsAny(supportedVersions),
                allowedVersions, supportedVersions, null)
        }

        return MessageBuilderResult(false)
    }

}
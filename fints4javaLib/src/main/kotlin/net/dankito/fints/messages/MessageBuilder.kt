package net.dankito.fints.messages

import net.dankito.fints.messages.datenelemente.implementierte.Aufsetzpunkt
import net.dankito.fints.messages.datenelemente.implementierte.Synchronisierungsmodus
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedienArtVersion
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanMediumKlasse
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanProcess
import net.dankito.fints.messages.segmente.ISegmentNumberGenerator
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.SegmentNumberGenerator
import net.dankito.fints.messages.segmente.Synchronisierung
import net.dankito.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.fints.messages.segmente.implementierte.*
import net.dankito.fints.messages.segmente.implementierte.sepa.SepaEinzelueberweisung
import net.dankito.fints.messages.segmente.implementierte.tan.TanGeneratorListeAnzeigen
import net.dankito.fints.messages.segmente.implementierte.tan.TanGeneratorTanMediumAnOderUmmelden
import net.dankito.fints.messages.segmente.implementierte.umsaetze.KontoumsaetzeZeitraumMt940Version5
import net.dankito.fints.messages.segmente.implementierte.umsaetze.KontoumsaetzeZeitraumMt940Version6
import net.dankito.fints.messages.segmente.implementierte.umsaetze.KontoumsaetzeZeitraumMt940Version7
import net.dankito.fints.messages.segmente.implementierte.umsaetze.Saldenabfrage
import net.dankito.fints.model.*
import net.dankito.fints.response.segments.JobParameters
import net.dankito.fints.response.segments.SepaAccountInfoParameters
import net.dankito.fints.response.segments.TanResponse
import net.dankito.fints.util.FinTsUtils
import net.dankito.utils.extensions.containsAny
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


    var lastCreatedMessage: MessageBuilderResult? = null
        protected set


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
                                          account: AccountData, product: ProductData, dialogData: DialogData): MessageBuilderResult {

        val result = supportsGetTransactionsMt940(account)

        if (result.isJobVersionSupported) {
            val transactionsJob = if (result.isAllowed(7)) KontoumsaetzeZeitraumMt940Version7(generator.resetSegmentNumber(2), parameter, bank, account)
            else if (result.isAllowed(6)) KontoumsaetzeZeitraumMt940Version6(generator.resetSegmentNumber(2), parameter, account)
            else KontoumsaetzeZeitraumMt940Version5(generator.resetSegmentNumber(2), parameter, account)

            val segments = listOf(
                transactionsJob,
                ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, CustomerSegmentId.AccountTransactionsMt940)
            )

            return createMessageBuilderResult(bank, customer, dialogData, segments)
        }

        return result
    }

    open fun supportsGetTransactions(account: AccountData): Boolean {
        return supportsGetTransactionsMt940(account).isJobVersionSupported
    }

    protected open fun supportsGetTransactionsMt940(account: AccountData): MessageBuilderResult {
        return getSupportedVersionsOfJob(CustomerSegmentId.AccountTransactionsMt940, account, listOf(5, 6, 7))
    }


    open fun createGetBalanceMessage(bank: BankData, customer: CustomerData, account: AccountData, product: ProductData, dialogData: DialogData): MessageBuilderResult {

        val result = supportsGetBalanceMessage(account)

        if (result.isJobVersionSupported) {
            val segments = listOf(
                Saldenabfrage(generator.resetSegmentNumber(2), account),
                ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, CustomerSegmentId.Balance)
            )

            return createMessageBuilderResult(bank, customer, dialogData, segments)
        }

        return result
    }

    open fun supportsGetBalance(account: AccountData): Boolean {
        return supportsGetBalanceMessage(account).isJobVersionSupported
    }

    protected open fun supportsGetBalanceMessage(account: AccountData): MessageBuilderResult {
        return getSupportedVersionsOfJob(CustomerSegmentId.Balance, account, listOf(5))
    }


    open fun createGetTanMediaListMessage(bank: BankData, customer: CustomerData, dialogData: DialogData,
                                          tanMediaKind: TanMedienArtVersion = TanMedienArtVersion.Alle,
                                          tanMediumClass: TanMediumKlasse = TanMediumKlasse.AlleMedien): MessageBuilderResult {

        val result = getSupportedVersionsOfJob(CustomerSegmentId.TanMediaList, customer, listOf(2, 3, 4, 5))

        if (result.isJobVersionSupported) {
            val segments = listOf(
                TanGeneratorListeAnzeigen(result.getHighestAllowedVersion!!,
                    generator.resetSegmentNumber(2), tanMediaKind, tanMediumClass)
            )

            return createMessageBuilderResult(bank, customer, dialogData, segments)
        }

        return result
    }

    open fun createChangeTanMediumMessage(newActiveTanMedium: TanGeneratorTanMedium, bank: BankData, customer: CustomerData,
                                          dialogData: DialogData, tan: String? = null, atc: Int? = null): MessageBuilderResult {

        val result = getSupportedVersionsOfJob(CustomerSegmentId.ChangeTanMedium, customer, listOf(1, 2, 3))

        if (result.isJobVersionSupported) {
            val segments = listOf(
                TanGeneratorTanMediumAnOderUmmelden(result.getHighestAllowedVersion!!, generator.resetSegmentNumber(2),
                    bank, customer, newActiveTanMedium, tan, atc)
            )

            return createMessageBuilderResult(bank, customer, dialogData, segments)
        }

        return result
    }

    open fun createSendEnteredTanMessage(enteredTan: String, tanResponse: TanResponse, bank: BankData, customer: CustomerData, dialogData: DialogData): String {

        val tanProcess = if (tanResponse.tanProcess == TanProcess.TanProcess1) TanProcess.TanProcess1 else TanProcess.TanProcess2

        return createSignedMessage(bank, customer, dialogData, enteredTan, listOf(
            ZweiSchrittTanEinreichung(generator.resetSegmentNumber(2), tanProcess, null,
                tanResponse.jobHashValue, tanResponse.jobReference, false, null, tanResponse.tanMediaIdentifier)
        ))
    }


    open fun createBankTransferMessage(bankTransferData: BankTransferData, bank: BankData, customer: CustomerData, account: AccountData, dialogData: DialogData): MessageBuilderResult {

        val messageBuilderResultAndNullableUrn = supportsBankTransferAndSepaVersion(account)
        val result = messageBuilderResultAndNullableUrn.first
        val urn = messageBuilderResultAndNullableUrn.second

        if (result.isJobVersionSupported && urn != null) {
            val segments = listOf(
                SepaEinzelueberweisung(generator.resetSegmentNumber(2), urn, customer, account, bank.bic, bankTransferData),
                ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, CustomerSegmentId.SepaBankTransfer)
            )

            return createMessageBuilderResult(bank, customer, dialogData, segments)
        }

        return result
    }

    open fun supportsBankTransfer(account: AccountData): Boolean {
        return supportsBankTransferAndSepaVersion(account).first.isJobVersionSupported
    }

    protected open fun supportsBankTransferAndSepaVersion(account: AccountData): Pair<MessageBuilderResult, String?> {
        val result = getSupportedVersionsOfJob(CustomerSegmentId.SepaBankTransfer, account, listOf(1))

        if (result.isJobVersionSupported) {

            getSepaUrnFor(CustomerSegmentId.SepaAccountInfoParameters, account, "pain.001.001.03")?.let { urn ->
                return Pair(result, urn)
            }

            return Pair(MessageBuilderResult(true, false, result.allowedVersions, result.supportedVersions, null), null) // TODO: how to tell that we don't support required SEPA pain version?
        }

        return Pair(result, null)
    }


    open fun rebuildMessageWithContinuationId(message: MessageBuilderResult, continuationId: String, bank: BankData,
                                              customer: CustomerData, dialogData: DialogData): MessageBuilderResult? {

//        val copiedSegments = message.messageBodySegments.map {  }
        val aufsetzpunkte = message.messageBodySegments.flatMap { it.dataElementsAndGroups }.filterIsInstance<Aufsetzpunkt>()

        if (aufsetzpunkte.isEmpty()) {
//            return MessageBuilderResult(message.isJobAllowed, message.isJobVersionSupported, message.allowedVersions, message.supportedVersions, null)
            return null
        }

        aufsetzpunkte.forEach { it.resetContinuationId(continuationId) }

        return rebuildMessage(message, bank, customer, dialogData)
    }

    open fun rebuildMessage(message: MessageBuilderResult, bank: BankData, customer: CustomerData,
                            dialogData: DialogData): MessageBuilderResult {

        dialogData.increaseMessageNumber()

        return createMessageBuilderResult(bank, customer, dialogData, message.messageBodySegments)
    }

    protected open fun createMessageBuilderResult(bank: BankData, customer: CustomerData, dialogData: DialogData, segments: List<Segment>): MessageBuilderResult {
        val message = MessageBuilderResult(createSignedMessage(bank, customer, dialogData, segments), segments)

        lastCreatedMessage = message

        return message
    }


    open fun createSignedMessage(bank: BankData, customer: CustomerData, dialogData: DialogData,
                                 payloadSegments: List<Segment>): String {

        return createSignedMessage(bank, customer, dialogData, null, payloadSegments)
    }

    open fun createSignedMessage(bank: BankData, customer: CustomerData, dialogData: DialogData,
                                 tan: String? = null, payloadSegments: List<Segment>): String {

        val date = utils.formatDateTodayAsInt()
        val time = utils.formatTimeNowAsInt()

        val signedPayload = signPayload(2, bank, customer, date, time, tan, payloadSegments)

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
                                   tan: String? = null, payloadSegments: List<Segment>): List<Segment> {

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
            customer.pin,
            tan
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


    protected open fun getSupportedVersionsOfJob(segmentId: CustomerSegmentId, account: AccountData,
                                                 supportedVersions: List<Int>): MessageBuilderResult {

        val allowedJobs = getAllowedJobs(segmentId, account)

        return getSupportedVersionsOfJob(supportedVersions, allowedJobs)
    }

    // TODO: try to get rid of
    protected open fun getSupportedVersionsOfJob(segmentId: CustomerSegmentId, customer: CustomerData,
                                                 supportedVersions: List<Int>): MessageBuilderResult {

        val allowedJobs = getAllowedJobs(segmentId, customer)

        return getSupportedVersionsOfJob(supportedVersions, allowedJobs)
    }

    protected open fun getSupportedVersionsOfJob(supportedVersions: List<Int>, allowedJobs: List<JobParameters>): MessageBuilderResult {
        if (allowedJobs.isNotEmpty()) {
            val allowedVersions = allowedJobs
                .map { it.segmentVersion }
                .sortedDescending()

            return MessageBuilderResult(
                allowedVersions.isNotEmpty(), allowedVersions.containsAny(supportedVersions),
                allowedVersions, supportedVersions, null
            )
        }

        return MessageBuilderResult(false)
    }

    protected open fun getSepaUrnFor(segmentId: CustomerSegmentId, account: AccountData, sepaDataFormat: String): String? {

        return getAllowedJobs(segmentId, account)
            .filterIsInstance<SepaAccountInfoParameters>()
            .sortedByDescending { it.segmentVersion }
            .flatMap { it.supportedSepaFormats }
            .firstOrNull { it.contains(sepaDataFormat) }
    }

    protected open fun getAllowedJobs(segmentId: CustomerSegmentId, account: AccountData): List<JobParameters> {

        return account.allowedJobs.filter { it.jobName == segmentId.id }
    }

    // TODO: this implementation is in most cases wrong, try to get rid of
    protected open fun getAllowedJobs(segmentId: CustomerSegmentId, customer: CustomerData): List<JobParameters> {

        return customer.accounts.flatMap { account ->
            return account.allowedJobs.filter { it.jobName == segmentId.id }
        }
    }

}
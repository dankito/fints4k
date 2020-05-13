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
import net.dankito.fints.messages.segmente.implementierte.sepa.SepaBankTransferBase
import net.dankito.fints.messages.segmente.implementierte.tan.TanGeneratorListeAnzeigen
import net.dankito.fints.messages.segmente.implementierte.tan.TanGeneratorTanMediumAnOderUmmelden
import net.dankito.fints.messages.segmente.implementierte.umsaetze.*
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


    /**
     * Um Kunden die Möglichkeit zu geben, sich anonym anzumelden, um sich bspw. über die
     * angebotenen Geschäftsvorfälle fremder Kreditinstitute (von denen sie keine BPD besitzen)
     * zu informieren bzw. nicht-signierungspflichtige Aufträge bei fremden Kreditinstituten
     * einreichen zu können, kann sich der Kunde anonym (als Gast) anmelden.
     *
     * Bei anonymen Dialogen werden Nachrichten weder signiert, noch können sie verschlüsselt und komprimiert werden.
     */
    open fun createAnonymousDialogInitMessage(dialogContext: DialogContext): MessageBuilderResult {

        return createUnsignedMessageBuilderResult(dialogContext, listOf(
            IdentifikationsSegment(generator.resetSegmentNumber(1), dialogContext),
            Verarbeitungsvorbereitung(generator.getNextSegmentNumber(), dialogContext)
        ))
    }

    open fun createAnonymousDialogEndMessage(dialogContext: DialogContext): String {

        return createMessage(dialogContext, listOf(
            Dialogende(generator.resetSegmentNumber(1), dialogContext)
        ))
    }


    open fun createInitDialogMessage(dialogContext: DialogContext, useStrongAuthentication: Boolean = true): MessageBuilderResult {

        val segments = mutableListOf(
            IdentifikationsSegment(generator.resetSegmentNumber(2), dialogContext),
            Verarbeitungsvorbereitung(generator.getNextSegmentNumber(), dialogContext)
        )

        if (useStrongAuthentication) {
            segments.add(ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, CustomerSegmentId.Identification))
        }

        return createMessageBuilderResult(dialogContext, segments)
    }

    open fun createSynchronizeCustomerSystemIdMessage(dialogContext: DialogContext): MessageBuilderResult {

        return createMessageBuilderResult(dialogContext, listOf(
            IdentifikationsSegment(generator.resetSegmentNumber(2), dialogContext),
            Verarbeitungsvorbereitung(generator.getNextSegmentNumber(), dialogContext),
            ZweiSchrittTanEinreichung(generator.getNextSegmentNumber(), TanProcess.TanProcess4, CustomerSegmentId.Identification),
            Synchronisierung(generator.getNextSegmentNumber(), Synchronisierungsmodus.NeueKundensystemIdZurueckmelden)
        ))
    }

    open fun createDialogEndMessage(dialogContext: DialogContext): String {

        return createSignedMessage(dialogContext, listOf(
            Dialogende(generator.resetSegmentNumber(2), dialogContext)
        ))
    }


    open fun createGetTransactionsMessage(parameter: GetTransactionsParameter, account: AccountData,
                                          dialogContext: DialogContext): MessageBuilderResult {

        val result = supportsGetTransactionsMt940(account)

        if (result.isJobVersionSupported) {
            val transactionsJob = if (result.isAllowed(7)) KontoumsaetzeZeitraumMt940Version7(generator.resetSegmentNumber(2), parameter, dialogContext.bank, account)
            else if (result.isAllowed(6)) KontoumsaetzeZeitraumMt940Version6(generator.resetSegmentNumber(2), parameter, account)
            else KontoumsaetzeZeitraumMt940Version5(generator.resetSegmentNumber(2), parameter, account)

            val segments = mutableListOf<Segment>(transactionsJob)

            addTanSegmentIfRequired(CustomerSegmentId.AccountTransactionsMt940, dialogContext, segments)

            return createMessageBuilderResult(dialogContext, segments)
        }

        return result
    }

    open fun supportsGetTransactions(account: AccountData): Boolean {
        return supportsGetTransactionsMt940(account).isJobVersionSupported
    }

    protected open fun supportsGetTransactionsMt940(account: AccountData): MessageBuilderResult {
        return getSupportedVersionsOfJob(CustomerSegmentId.AccountTransactionsMt940, account, listOf(5, 6, 7))
    }


    open fun createGetBalanceMessage(account: AccountData, dialogContext: DialogContext): MessageBuilderResult {

        val result = supportsGetBalanceMessage(account)

        if (result.isJobVersionSupported) {
            val balanceJob = if (result.isAllowed(5)) SaldenabfrageVersion5(generator.resetSegmentNumber(2), account)
            else SaldenabfrageVersion7(generator.resetSegmentNumber(2), account, dialogContext.bank)

            val segments = mutableListOf<Segment>(balanceJob)

            addTanSegmentIfRequired(CustomerSegmentId.Balance, dialogContext, segments)

            return createMessageBuilderResult(dialogContext, segments)
        }

        return result
    }

    open fun supportsGetBalance(account: AccountData): Boolean {
        return supportsGetBalanceMessage(account).isJobVersionSupported
    }

    protected open fun supportsGetBalanceMessage(account: AccountData): MessageBuilderResult {
        return getSupportedVersionsOfJob(CustomerSegmentId.Balance, account, listOf(5, 7))
    }


    open fun createGetTanMediaListMessage(dialogContext: DialogContext,
                                          tanMediaKind: TanMedienArtVersion = TanMedienArtVersion.Alle,
                                          tanMediumClass: TanMediumKlasse = TanMediumKlasse.AlleMedien): MessageBuilderResult {

        val result = getSupportedVersionsOfJob(CustomerSegmentId.TanMediaList, dialogContext.customer, listOf(2, 3, 4, 5))

        if (result.isJobVersionSupported) {
            val segments = listOf(
                TanGeneratorListeAnzeigen(result.getHighestAllowedVersion!!,
                    generator.resetSegmentNumber(2), tanMediaKind, tanMediumClass)
            )

            return createMessageBuilderResult(dialogContext, segments)
        }

        return result
    }

    open fun createChangeTanMediumMessage(newActiveTanMedium: TanGeneratorTanMedium, dialogContext: DialogContext,
                                          tan: String? = null, atc: Int? = null): MessageBuilderResult {

        val result = getSupportedVersionsOfJob(CustomerSegmentId.ChangeTanMedium, dialogContext.customer, listOf(1, 2, 3))

        if (result.isJobVersionSupported) {
            val segments = listOf(
                TanGeneratorTanMediumAnOderUmmelden(result.getHighestAllowedVersion!!, generator.resetSegmentNumber(2),
                    dialogContext.bank, dialogContext.customer, newActiveTanMedium, tan, atc)
            )

            return createMessageBuilderResult(dialogContext, segments)
        }

        return result
    }

    open fun createSendEnteredTanMessage(enteredTan: String, tanResponse: TanResponse, dialogContext: DialogContext): String {

        val tanProcess = if (tanResponse.tanProcess == TanProcess.TanProcess1) TanProcess.TanProcess1 else TanProcess.TanProcess2

        return createSignedMessage(dialogContext, enteredTan, listOf(
            ZweiSchrittTanEinreichung(generator.resetSegmentNumber(2), tanProcess, null,
                tanResponse.jobHashValue, tanResponse.jobReference, false, null, tanResponse.tanMediaIdentifier)
        ))
    }


    open fun createBankTransferMessage(data: BankTransferData, account: AccountData, dialogContext: DialogContext): MessageBuilderResult {

        val segmentId = if (data.instantPayment) CustomerSegmentId.SepaInstantPaymentBankTransfer else CustomerSegmentId.SepaBankTransfer

        val messageBuilderResultAndNullableUrn = supportsBankTransferAndSepaVersion(account, segmentId)
        val result = messageBuilderResultAndNullableUrn.first
        val urn = messageBuilderResultAndNullableUrn.second

        if (result.isJobVersionSupported && urn != null) {
            val segments = mutableListOf<Segment>(SepaBankTransferBase(segmentId, generator.resetSegmentNumber(2),
                urn, dialogContext.customer, account, dialogContext.bank.bic, data))

            addTanSegmentIfRequired(segmentId, dialogContext, segments)

            return createMessageBuilderResult(dialogContext, segments)
        }

        return result
    }

    open fun supportsBankTransfer(account: AccountData): Boolean {
        return supportsBankTransferAndSepaVersion(account, CustomerSegmentId.SepaBankTransfer).first.isJobVersionSupported
    }

    open fun supportsSepaInstantPaymentBankTransfer(account: AccountData): Boolean {
        return supportsBankTransferAndSepaVersion(account, CustomerSegmentId.SepaInstantPaymentBankTransfer).first.isJobVersionSupported
    }

    protected open fun supportsBankTransferAndSepaVersion(account: AccountData, segmentId: CustomerSegmentId): Pair<MessageBuilderResult, String?> {
        val result = getSupportedVersionsOfJob(segmentId, account, listOf(1))

        if (result.isJobVersionSupported) {

            getSepaUrnFor(CustomerSegmentId.SepaAccountInfoParameters, account, "pain.001.001.03")?.let { urn ->
                return Pair(result, urn)
            }

            getSepaUrnFor(CustomerSegmentId.SepaAccountInfoParameters, account, "pain.001.003.03")?.let { urn ->
                return Pair(result, urn)
            }

            return Pair(MessageBuilderResult(true, false, result.allowedVersions, result.supportedVersions, null), null) // TODO: how to tell that we don't support required SEPA pain version?
        }

        return Pair(result, null)
    }


    open fun rebuildMessageWithContinuationId(message: MessageBuilderResult, continuationId: String, dialogContext: DialogContext): MessageBuilderResult? {

//        val copiedSegments = message.messageBodySegments.map {  }
        val aufsetzpunkte = message.messageBodySegments.flatMap { it.dataElementsAndGroups }.filterIsInstance<Aufsetzpunkt>()

        if (aufsetzpunkte.isEmpty()) {
//            return MessageBuilderResult(message.isJobAllowed, message.isJobVersionSupported, message.allowedVersions, message.supportedVersions, null)
            return null
        }

        aufsetzpunkte.forEach { it.resetContinuationId(continuationId) }

        return rebuildMessage(message, dialogContext)
    }

    open fun rebuildMessage(message: MessageBuilderResult, dialogContext: DialogContext): MessageBuilderResult {

        dialogContext.increaseMessageNumber()

        return createMessageBuilderResult(dialogContext, message.messageBodySegments)
    }

    protected open fun createMessageBuilderResult(dialogContext: DialogContext, segments: List<Segment>): MessageBuilderResult {
        val message = MessageBuilderResult(createSignedMessage(dialogContext, segments), segments)

        dialogContext.previousMessageInDialog = dialogContext.currentMessage

        dialogContext.currentMessage = message

        return message
    }

    protected open fun createUnsignedMessageBuilderResult(dialogContext: DialogContext, segments: List<Segment>): MessageBuilderResult {
        val message = MessageBuilderResult(createMessage(dialogContext, segments), segments)

        dialogContext.previousMessageInDialog = dialogContext.currentMessage

        dialogContext.currentMessage = message

        return message
    }


    open fun createSignedMessage(dialogContext: DialogContext, payloadSegments: List<Segment>): String {

        return createSignedMessage(dialogContext, null, payloadSegments)
    }

    open fun createSignedMessage(dialogContext: DialogContext, tan: String? = null,
                                 payloadSegments: List<Segment>): String {

        val date = utils.formatDateTodayAsInt()
        val time = utils.formatTimeNowAsInt()

        val signedPayload = signPayload(2, dialogContext, date, time, tan, payloadSegments)

        val encryptedPayload = encryptPayload(dialogContext, date, time, signedPayload)

        return createMessage(dialogContext, encryptedPayload)
    }

    open fun createMessage(dialogContext: DialogContext, payloadSegments: List<Segment>): String {

        dialogContext.increaseMessageNumber()

        val formattedPayload = formatPayload(payloadSegments)

        val messageSize = formattedPayload.length + MessageHeaderLength + MessageEndingLength + AddedSeparatorsLength

        val header = Nachrichtenkopf(ISegmentNumberGenerator.FirstSegmentNumber, messageSize, dialogContext)

        val ending = Nachrichtenabschluss(generator.getNextSegmentNumber(), dialogContext)

        return listOf(header.format(), formattedPayload, ending.format())
            .joinToString(Separators.SegmentSeparator, postfix = Separators.SegmentSeparator)
    }


    protected open fun signPayload(headerSegmentNumber: Int, dialogContext: DialogContext, date: Int, time: Int,
                                   tan: String? = null, payloadSegments: List<Segment>): List<Segment> {

        val controlReference = createControlReference()

        val signatureHeader = PinTanSignaturkopf(
            headerSegmentNumber,
            dialogContext,
            controlReference,
            date,
            time
        )

        val signatureEnding = Signaturabschluss(
            generator.getNextSegmentNumber(),
            controlReference,
            dialogContext.customer.pin,
            tan
        )

        return listOf(signatureHeader, *payloadSegments.toTypedArray(), signatureEnding)
    }

    protected open fun createControlReference(): String {
        return Math.abs(Random(System.nanoTime()).nextInt()).toString()
    }


    private fun encryptPayload(dialogContext: DialogContext, date: Int, time: Int,
                               payload: List<Segment>): List<Segment> {

        val encryptionHeader = PinTanVerschluesselungskopf(dialogContext, date, time)

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

    protected open fun addTanSegmentIfRequired(segmentId: CustomerSegmentId, dialogContext: DialogContext, segments: MutableList<Segment>) {
        if (isTanRequiredForJob(segmentId, dialogContext)) {
            segments.add(ZweiSchrittTanEinreichung(
                generator.getNextSegmentNumber(), TanProcess.TanProcess4, segmentId))
        }
    }

    protected open fun isTanRequiredForJob(segmentId: CustomerSegmentId, dialogContext: DialogContext): Boolean {
        return dialogContext.bank.pinInfo?.jobTanConfiguration?.first { it.segmentId == segmentId.id }?.tanRequired
            ?: false // TODO: actually in this case it's not allowed to execute job via PIN/TAN at all
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
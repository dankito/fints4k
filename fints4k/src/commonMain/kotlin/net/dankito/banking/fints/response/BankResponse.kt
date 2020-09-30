package net.dankito.banking.fints.response

import net.dankito.banking.fints.messages.MessageBuilderResult
import net.dankito.banking.fints.messages.Separators
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.banking.fints.messages.segmente.id.ISegmentId
import net.dankito.banking.fints.messages.segmente.id.MessageSegmentId
import net.dankito.banking.fints.response.segments.*


open class BankResponse(
    val didReceiveResponse: Boolean,
    val receivedResponse: String? = null,
    val receivedSegments: List<ReceivedSegment> = listOf(),

    /**
     * When a serious error occurred during web request or response parsing.
     */
    val errorMessage: String? = null,
    val noTanMethodSelected: Boolean = false,
    val messageCreationError: MessageBuilderResult? = null
) {

    open val couldCreateMessage: Boolean
        get() = messageCreationError == null

    open val responseContainsErrors: Boolean
        get() = errorMessage == null && messageFeedback?.isError == true

    open var tanRequiredButUserDidNotEnterOne = false

    open var tanRequiredButWeWereToldToAbortIfSo = false

    open val successful: Boolean
        get() = noTanMethodSelected == false && couldCreateMessage && didReceiveResponse
                && responseContainsErrors == false && tanRequiredButUserDidNotEnterOne == false
                && tanRequiredButWeWereToldToAbortIfSo == false

    open val isStrongAuthenticationRequired: Boolean
        get() = tanResponse?.isStrongAuthenticationRequired == true

    open val tanResponse: TanResponse?
        get() = getFirstSegmentById(InstituteSegmentId.Tan)


    open val messageHeader: ReceivedMessageHeader?
        get() = getFirstSegmentById(MessageSegmentId.MessageHeader)

    open val messageFeedback: MessageFeedback?
        get() = getFirstSegmentById(InstituteSegmentId.MessageFeedback)

    open val didBankCloseDialog: Boolean
        get() = messageFeedback?.feedbacks?.firstOrNull { it.responseCode == 9800 } != null

    open val segmentFeedbacks: List<SegmentFeedback>
        get() = getSegmentsById(InstituteSegmentId.SegmentFeedback)
    
    open val aufsetzpunkt: String? // TODO: what to do if there are multiple Aufsetzpunkte?
        get() = segmentFeedbacks.flatMap { it.feedbacks }.filterIsInstance<AufsetzpunktFeedback>().firstOrNull()?.aufsetzpunkt

    open val errorsToShowToUser: List<String>
        get() {
            val errorMessages = segmentFeedbacks
                .flatMap { it.feedbacks }
                .mapNotNull { mapToMessageToShowToUser(it) }
                .toMutableList()

            messageFeedback?.let { messageFeedback ->
                if (messageFeedback.isError) {
                    errorMessages.addAll(0, messageFeedback.feedbacks.mapNotNull { mapToMessageToShowToUser(it) })
                }
            }

            return errorMessages
        }

    protected open fun mapToMessageToShowToUser(feedback: Feedback): String? {
        if (feedback.isError) {
            return "${feedback.responseCode}: ${feedback.message}"
        }

        return null
    }


    open var followUpResponse: BankResponse? = null

    open var hasFollowUpMessageButCouldNotReceiveIt: Boolean? = false


    /**
     * Returns an empty list of response didn't contain any job parameters.
     *
     * Returns all jobs bank supports otherwise. This does not necessarily mean that they are also allowed for
     * customer / account, see [net.dankito.fints.model.AccountData.allowedJobNames].
     */
    open val supportedJobs: List<JobParameters>
        get() = receivedSegments.mapNotNull { it as? JobParameters }

    open val supportedTanMethodsForUser: List<Sicherheitsfunktion>
        get() = segmentFeedbacks.flatMap { it.feedbacks }
                                .filterIsInstance<SupportedTanMethodsForUserFeedback>()
                                .flatMap { it.supportedTanMethods }


    open fun <T : ReceivedSegment> getFirstSegmentById(id: ISegmentId): T? {
        return getFirstSegmentById(id.id)
    }

    open fun <T : ReceivedSegment> getFirstSegmentById(id: String): T? {
        return receivedSegments.firstOrNull { it.segmentId == id } as T?
    }

    open fun <T : ReceivedSegment> getSegmentsById(id: ISegmentId): List<T> {
        return getSegmentsById(id.id)
    }

    open fun <T : ReceivedSegment> getSegmentsById(id: String): List<T> {
        return receivedSegments.filter { it.segmentId == id }.mapNotNull { it as? T }
    }


    override fun toString(): String {
        val formattedResponse = receivedResponse?.replace(Separators.SegmentSeparator, "\r\n") ?: ""

        if (successful) {
            return formattedResponse
        }

        return "Error: $errorMessage\n$formattedResponse"
    }

}
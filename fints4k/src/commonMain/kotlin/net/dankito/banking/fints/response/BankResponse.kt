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

    open val wrongCredentialsEntered: Boolean
        get() {
            return messageFeedback?.feedbacks?.any { isWrongCredentialsEnteredFeedback(it) } == true
                    || segmentFeedbacks.flatMap { it.feedbacks }.any { isWrongCredentialsEnteredFeedback(it) }
        }

    protected open fun isWrongCredentialsEnteredFeedback(feedback: Feedback): Boolean {
        if (feedback.responseCode == 9340) {
            return true
        }

        if (feedback.responseCode in 9910..9949) { // this is not 100 % correct, there are e.g. messages like "9941 TAN ungültig" or "9910 Chipkarte gesperrt", see p. 22-23 FinTS_Rueckmeldungscodes ->
            return feedback.message.contains("TAN", true) == false && feedback.message.contains("Chipkarte", true) == false // ... try to filter these
        }

        if (feedback.responseCode == 9210) { // there are many, many different messages with response code 9210, try to find these with 'Unbekannter Benutzer', 'Benutzerkennung ungültig', 'Bitte korrigieren Sie Ihre Zugangsdaten'
            return feedback.message.contains("Unbekannt", true) || feedback.message.contains("kennung", true)
                    || feedback.message.contains("Zugangsdaten", true)
        }

        return false
    }

    open var tanRequiredButUserDidNotEnterOne = false

    open var tanRequiredButWeWereToldToAbortIfSo = false

    open val successful: Boolean
        get() = noTanMethodSelected == false && couldCreateMessage && didReceiveResponse
                && responseContainsErrors == false && wrongCredentialsEntered == false
                && tanRequiredButUserDidNotEnterOne == false && tanRequiredButWeWereToldToAbortIfSo == false

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
            if (feedback.responseCode != 9800) { // filter out 'Dialogabbruch' feedbacks, is of no value to user
                return "${feedback.responseCode}: ${feedback.message}"
            }
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
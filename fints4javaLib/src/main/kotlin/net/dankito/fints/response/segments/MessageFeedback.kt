package net.dankito.fints.response.segments


/**
 * In diesem Segment werden Rückmeldungen übermittelt, die sich auf die gesamte Nachricht und nicht auf ein
 * spezifisches Segment beziehen (z. B. „Nachricht entgegengenommen“, "Elektronische Signatur gesperrt").
 */
open class MessageFeedback(
    val feedbacks: List<Feedback>,
    segmentString: String
)
    : ReceivedSegment(segmentString) {


    val isSuccess: Boolean
        get() = isError == false && isWarning == false

    val isWarning: Boolean
        get() = isError == false && feedbacks.firstOrNull { it.isWarning } != null

    val isError: Boolean
        get() = feedbacks.firstOrNull { it.isError } != null


    override fun toString(): String {
        return feedbacks.joinToString("\n")
    }

}
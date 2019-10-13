package net.dankito.fints.response

import net.dankito.fints.messages.Separators
import net.dankito.fints.messages.segmente.id.ISegmentId
import net.dankito.fints.messages.segmente.id.MessageSegmentId
import net.dankito.fints.response.segments.*


open class Response constructor(
    val didReceiveResponse: Boolean,
    val receivedResponse: String? = null,
    val receivedSegments: List<ReceivedSegment> = listOf(),

    /**
     * When a serious error occurred during web request or response parsing.
     */
    val exception: Exception? = null
) {

    open val responseContainsErrors: Boolean
        get() = exception == null && messageFeedback?.isError == true

    open val successful: Boolean
        get() = didReceiveResponse && responseContainsErrors == false

    open val isStrongAuthenticationRequired: Boolean
        get() = tanResponse?.isStrongAuthenticationRequired == true

    open val tanResponse: TanResponse?
        get() = getFirstSegmentById(InstituteSegmentId.Tan)


    open val messageHeader: ReceivedMessageHeader?
        get() = getFirstSegmentById(MessageSegmentId.MessageHeader)

    open val messageFeedback: MessageFeedback?
        get() = getFirstSegmentById(InstituteSegmentId.MessageFeedback)

    open val segmentFeedbacks: List<SegmentFeedback>
        get() = getSegmentsById(InstituteSegmentId.SegmentFeedback)

    open val errorsToShowToUser: List<String>
        get() {
            val errorMessages = segmentFeedbacks
                .flatMap { it.feedbacks }
                .mapNotNull { if (it.isError) it.message else null }
                .toMutableList()

            messageFeedback?.let { messageFeedback ->
                if (messageFeedback.isError) {
                    errorMessages.addAll(0, messageFeedback.feedbacks.mapNotNull { if (it.isError) it.message else null })
                }
            }

            return errorMessages
        }


    /**
     * Returns an empty list of response didn't contain any allowed jobs.
     *
     * Returns all jobs bank supports otherwise. This does not necessarily mean that they are also allowed for
     * customer / account, see [net.dankito.fints.model.AccountData.allowedJobNames].
     */
    open val allowedJobs: List<AllowedJob>
        get() = receivedSegments.mapNotNull { it as? AllowedJob }


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
        val formattedResponse = receivedResponse?.replace(Separators.SegmentSeparator, System.lineSeparator()) ?: ""

        if (successful) {
            return formattedResponse
        }

        return "Error: $exception\n$formattedResponse"
    }

}
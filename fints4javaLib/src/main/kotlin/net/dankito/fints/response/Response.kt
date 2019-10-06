package net.dankito.fints.response

import net.dankito.fints.messages.Separators
import net.dankito.fints.messages.segmente.id.ISegmentId
import net.dankito.fints.messages.segmente.id.MessageSegmentId
import net.dankito.fints.response.segments.ReceivedMessageHeader
import net.dankito.fints.response.segments.ReceivedSegment


open class Response(
    val didReceiveResponse: Boolean,
    val didResponseContainErrors: Boolean,
    val receivedResponse: String? = null,
    val receivedSegments: List<ReceivedSegment> = listOf(),
    val error: Exception? = null
) {

    open val successful: Boolean
        get() = didReceiveResponse && didResponseContainErrors == false


    open val messageHeader: ReceivedMessageHeader?
        get() = getFirstSegmentById(MessageSegmentId.MessageHeader)

    open fun <T : ReceivedSegment> getFirstSegmentById(id: ISegmentId): T? {
        return getFirstSegmentById(id.id)
    }

    open fun <T : ReceivedSegment> getFirstSegmentById(id: String): T? {
        return receivedSegments.firstOrNull { it.segmentId == id } as T?
    }

    open fun getSegmentsById(id: ISegmentId): List<ReceivedSegment> {
        return getSegmentsById(id.id)
    }

    open fun getSegmentsById(id: String): List<ReceivedSegment> {
        return receivedSegments.filter { it.segmentId == id }
    }


    override fun toString(): String {
        val formattedResponse = receivedResponse?.replace(Separators.SegmentSeparator, System.lineSeparator()) ?: ""

        if (successful) {
            return formattedResponse
        }

        return "Error: $error\n$formattedResponse"
    }

}
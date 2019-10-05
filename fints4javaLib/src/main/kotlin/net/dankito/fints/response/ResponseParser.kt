package net.dankito.fints.response

import net.dankito.fints.messages.Separators
import net.dankito.fints.messages.segmente.id.MessageSegmentId
import net.dankito.fints.response.segments.ReceivedMessageHeader
import net.dankito.fints.response.segments.ReceivedSegment
import net.dankito.fints.response.segments.ReceivedSynchronization
import org.slf4j.LoggerFactory


open class ResponseParser {

    companion object {
        private val log = LoggerFactory.getLogger(ResponseParser::class.java)
    }


    open fun parse(response: String): Response {
        try {
            val segments = response.split(Separators.SegmentSeparator)

            val parsedSegments = segments.mapNotNull { parseSegment(it) }

            return Response(true, determineContainsErrors(parsedSegments), response, parsedSegments)
        } catch (e: Exception) {
            log.error("Could not parse response '$response'", e)

            return Response(true, true, response, error = e)
        }
    }


    protected open fun determineContainsErrors(parsedSegments: List<ReceivedSegment>): Boolean {
        return false // TODO
    }


    protected open fun parseSegment(segment: String): ReceivedSegment? {
        try {
            if (segment.isNotEmpty()) { // filter out empty lines
                val dataElementGroups = segment.split(Separators.DataElementGroupsSeparator)
                val segmentId = segment.substring(0, segment.indexOf(Separators.DataElementsSeparator))

                return parseSegment(segment, segmentId, dataElementGroups)
            }
        } catch (e: Exception) {
            log.error("Could not parse segment '$segment'", e) // TODO: what to do here, how to inform user?
        }

        return null
    }

    protected open fun parseSegment(segment: String, segmentId: String, dataElementGroups: List<String>): ReceivedSegment? {
        return when (segmentId) {
            MessageSegmentId.MessageHeader.id -> parseMessageHeaderSegment(segment, dataElementGroups)
            InstituteSegmentId.Synchronization.id -> parseSynchronization(segment, dataElementGroups)
            else -> null
        }
    }


    protected open fun parseMessageHeaderSegment(segment: String, dataElementGroups: List<String>): ReceivedMessageHeader {
        val messageSize = dataElementGroups[1].toInt()
        val finTsVersion = dataElementGroups[2].toInt()
        val dialogId = dataElementGroups[3]
        val messageNumber = dataElementGroups[4].toInt()

        return ReceivedMessageHeader(messageSize, finTsVersion, dialogId, messageNumber, segment)
    }

    protected open fun parseSynchronization(segment: String, dataElementGroups: List<String>): ReceivedSynchronization {
        val customerSystemId = dataElementGroups[1]
        val lastMessageNumber = if (dataElementGroups.size > 2) dataElementGroups[2] else null
        val securityReferenceNumberForSigningKey = if (dataElementGroups.size > 3) dataElementGroups[3] else null
        val securityReferenceNumberForDigitalSignature = if (dataElementGroups.size > 4) dataElementGroups[4] else null

        return ReceivedSynchronization(segment, customerSystemId, lastMessageNumber,
            securityReferenceNumberForSigningKey, securityReferenceNumberForDigitalSignature)
    }

}
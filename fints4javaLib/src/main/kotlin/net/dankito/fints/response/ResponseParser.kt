package net.dankito.fints.response

import net.dankito.fints.messages.Separators
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.HbciVersion
import net.dankito.fints.messages.datenelemente.implementierte.ICodeEnum
import net.dankito.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung
import net.dankito.fints.messages.segmente.id.MessageSegmentId
import net.dankito.fints.response.segments.BankParameters
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
            InstituteSegmentId.BankParameters.id -> parseBankParameters(segment, dataElementGroups)
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

    protected open fun parseBankParameters(segment: String, dataElementGroups: List<String>): BankParameters {
        val bpdVersion = dataElementGroups[1].toInt()
        val bankDetails = parseBankDetails(dataElementGroups[2])
        val bankName = dataElementGroups[3]

        val countMaxJobsPerMessage = dataElementGroups[4].toInt()
        val supportedLanguages = parseLanguages(dataElementGroups[5])
        val supportedHbciVersions = parseHbciVersions(dataElementGroups[6])

        val maxMessageSize = if (dataElementGroups.size > 7) dataElementGroups[7].toInt() else null
        val minTimeout = if (dataElementGroups.size > 8) dataElementGroups[8].toInt() else null
        val maxTimeout = if (dataElementGroups.size > 9) dataElementGroups[9].toInt() else null

        return BankParameters(bpdVersion, bankDetails.bankCountryCode, bankDetails.bankCode, bankName,
            countMaxJobsPerMessage, supportedLanguages, supportedHbciVersions, maxMessageSize, minTimeout, maxTimeout, segment)
    }


    protected open fun parseBankDetails(dataElementsGroup: String): Kreditinstitutskennung {
        val detailsStrings = getDataElements(dataElementsGroup)

        return Kreditinstitutskennung(detailsStrings[0].toInt(), detailsStrings[1])
    }

    protected open fun parseLanguages(dataElementsGroup: String): List<Dialogsprache> {
        val languageStrings = getDataElements(dataElementsGroup)

        return parseFromCode(languageStrings, Dialogsprache.values())
    }

    protected open fun parseHbciVersions(dataElementsGroup: String): List<HbciVersion> {
        val versionStrings = getDataElements(dataElementsGroup)

        return parseFromCode(versionStrings, HbciVersion.values())
    }

    protected open fun <T : ICodeEnum> parseFromCode(codeValues: List<String>, allValues: Array<T>): List<T> {
        // mapNotNull: don't crash if new, at time of implementation unknown values get introduced / returned by bank
        return codeValues.mapNotNull { code -> allValues.first { it.code == code } }
    }


    protected open fun getDataElements(dataElementGroup: String): List<String> {
        return dataElementGroup.split(Separators.DataElementsSeparator)
    }

}
package net.dankito.fints.response

import net.dankito.fints.messages.Separators
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.HbciVersion
import net.dankito.fints.messages.datenelemente.implementierte.ICodeEnum
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsverfahren
import net.dankito.fints.messages.datenelemente.implementierte.signatur.VersionDesSicherheitsverfahrens
import net.dankito.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung
import net.dankito.fints.messages.datenelementgruppen.implementierte.signatur.Sicherheitsprofil
import net.dankito.fints.messages.segmente.id.MessageSegmentId
import net.dankito.fints.response.segments.*
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
            InstituteSegmentId.SecurityMethods.id -> parseSecurityMethods(segment, dataElementGroups)

            InstituteSegmentId.UserParameters.id -> parseUserParameters(segment, dataElementGroups)
            InstituteSegmentId.AccountInfo.id -> parseAccountInfo(segment, dataElementGroups)

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

    protected open fun parseSecurityMethods(segment: String, dataElementGroups: List<String>): SecurityMethods {
        val mixingAllowed = parseBoolean(dataElementGroups[1])
        val profiles = parseSecurityProfiles(dataElementGroups.subList(2, dataElementGroups.size))

        return SecurityMethods(mixingAllowed, profiles, segment)
    }


    protected open fun parseUserParameters(segment: String, dataElementGroups: List<String>): UserParameters {
        val customerId = dataElementGroups[1]
        val updVersion = dataElementGroups[2].toInt()
        val areListedJobsBlocked = dataElementGroups[3] == "0"
        val username = if (dataElementGroups.size > 4) returnNullIfEmpty(dataElementGroups[4]) else null
        val extension = if (dataElementGroups.size > 5) returnNullIfEmpty(dataElementGroups[5]) else null

        return UserParameters(customerId, updVersion, areListedJobsBlocked, username, extension, segment)
    }

    protected open fun parseAccountInfo(segment: String, dataElementGroups: List<String>): AccountInfo {
        // this is parsing a Kontoverbindung. May extract a method for it.
        val accountDetails = getDataElements(dataElementGroups[1])
        val accountNumber = accountDetails[0]
        val subAccountAttribute = returnNullIfEmpty(accountDetails[1])
        val bankCountryCode = accountDetails[2].toInt()
        val bankCode = accountDetails[3]

        val iban = returnNullIfEmpty(dataElementGroups[2])
        val customerId = dataElementGroups[3]
        val accountType = parseCodeEnum(dataElementGroups[4], AccountTypeCode.values()).type
        val currency = dataElementGroups[5]
        val accountHolderName1 = dataElementGroups[6]
        val accountHolderName2 = returnNullIfEmpty(dataElementGroups[7])
        val productName = returnNullIfEmpty(dataElementGroups[8])
        val limit = returnNullIfEmpty(dataElementGroups[9]) // TODO: parse limit

        // TODO: parse allowed jobs
        // TODO: parse extension

        return AccountInfo(accountNumber, subAccountAttribute, bankCountryCode, bankCode, iban, customerId, accountType,
            currency, accountHolderName1, accountHolderName2, productName, limit, null, segment)
    }


    protected open fun parseBankDetails(dataElementsGroup: String): Kreditinstitutskennung {
        val detailsStrings = getDataElements(dataElementsGroup)

        return Kreditinstitutskennung(detailsStrings[0].toInt(), detailsStrings[1])
    }

    protected open fun parseLanguages(dataElementsGroup: String): List<Dialogsprache> {
        val languageStrings = getDataElements(dataElementsGroup)

        return parseCodeEnum(languageStrings, Dialogsprache.values())
    }

    protected open fun parseHbciVersions(dataElementsGroup: String): List<HbciVersion> {
        val versionStrings = getDataElements(dataElementsGroup)

        return parseCodeEnum(versionStrings, HbciVersion.values())
    }

    protected open fun parseSecurityProfiles(dataElementsGroups: List<String>): List<Sicherheitsprofil> {
        return dataElementsGroups.flatMap { dataElementGroup ->
            val parts = getDataElements(dataElementGroup)

            val method = parseSecurityMethod(parts[0])

            parts.subList(1, parts.size).map {
                Sicherheitsprofil(method, parseSecurityMethodVersion(it))
            }
        }
    }

    protected open fun parseSecurityMethod(methodString: String): Sicherheitsverfahren {
        return parseCodeEnum(methodString, Sicherheitsverfahren.values())
    }

    protected open fun parseSecurityMethodVersion(versionString: String): VersionDesSicherheitsverfahrens {
        val versionInt = versionString.toInt()

        return VersionDesSicherheitsverfahrens.values().first { it.methodNumber == versionInt }
    }

    protected open fun <T : ICodeEnum> parseCodeEnum(codeValues: List<String>, allValues: Array<T>): List<T> {
        // mapNotNull: don't crash if new, at time of implementation unknown values get introduced / returned by bank
        return codeValues.mapNotNull { code -> parseCodeEnum(code, allValues) }
    }

    protected open fun <T : ICodeEnum> parseCodeEnum(code: String, allValues: Array<T>): T {
        return  allValues.first { it.code == code }
    }


    protected open fun getDataElements(dataElementGroup: String): List<String> {
        return dataElementGroup.split(Separators.DataElementsSeparator)
    }

    protected open fun parseBoolean(dataElement: String): Boolean {
        if ("J" == dataElement) {
            return true
        }

        return false
    }

    protected open fun returnNullIfEmpty(string: String): String? {
        return if (string.isEmpty()) null else string
    }

}
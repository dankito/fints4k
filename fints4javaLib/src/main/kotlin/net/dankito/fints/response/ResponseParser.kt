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
            val segments = splitIntoPartsAndUnmask(response, Separators.SegmentSeparator)

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
                val dataElementGroups = splitIntoPartsAndUnmask(segment, Separators.DataElementGroupsSeparator)

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

            else -> UnparsedSegment(segment)
        }
    }


    protected open fun parseMessageHeaderSegment(segment: String, dataElementGroups: List<String>): ReceivedMessageHeader {
        val messageSize = parseInt(dataElementGroups[1])
        val finTsVersion = parseInt(dataElementGroups[2])
        val dialogId = parseString(dataElementGroups[3])
        val messageNumber = parseInt(dataElementGroups[4])

        return ReceivedMessageHeader(messageSize, finTsVersion, dialogId, messageNumber, segment)
    }

    protected open fun parseSynchronization(segment: String, dataElementGroups: List<String>): ReceivedSynchronization {
        val customerSystemId = parseString(dataElementGroups[1])
        val lastMessageNumber = if (dataElementGroups.size > 2) parseString(dataElementGroups[2]) else null
        val securityReferenceNumberForSigningKey = if (dataElementGroups.size > 3) parseString(dataElementGroups[3]) else null
        val securityReferenceNumberForDigitalSignature = if (dataElementGroups.size > 4) parseString(dataElementGroups[4]) else null

        return ReceivedSynchronization(segment, customerSystemId, lastMessageNumber,
            securityReferenceNumberForSigningKey, securityReferenceNumberForDigitalSignature)
    }

    protected open fun parseBankParameters(segment: String, dataElementGroups: List<String>): BankParameters {
        val bpdVersion = parseInt(dataElementGroups[1])
        val bankDetails = parseBankDetails(dataElementGroups[2])
        val bankName = parseString(dataElementGroups[3])

        val countMaxJobsPerMessage = parseInt(dataElementGroups[4])
        val supportedLanguages = parseLanguages(dataElementGroups[5])
        val supportedHbciVersions = parseHbciVersions(dataElementGroups[6])

        val maxMessageSize = if (dataElementGroups.size > 7) parseInt(dataElementGroups[7]) else null
        val minTimeout = if (dataElementGroups.size > 8) parseInt(dataElementGroups[8]) else null
        val maxTimeout = if (dataElementGroups.size > 9) parseInt(dataElementGroups[9]) else null

        return BankParameters(bpdVersion, bankDetails.bankCountryCode, bankDetails.bankCode, bankName,
            countMaxJobsPerMessage, supportedLanguages, supportedHbciVersions, maxMessageSize, minTimeout, maxTimeout, segment)
    }

    protected open fun parseSecurityMethods(segment: String, dataElementGroups: List<String>): SecurityMethods {
        val mixingAllowed = parseBoolean(dataElementGroups[1])
        val profiles = parseSecurityProfiles(dataElementGroups.subList(2, dataElementGroups.size))

        return SecurityMethods(mixingAllowed, profiles, segment)
    }


    protected open fun parseUserParameters(segment: String, dataElementGroups: List<String>): UserParameters {
        val customerId = parseString(dataElementGroups[1])
        val updVersion = parseInt(dataElementGroups[2])
        val areListedJobsBlocked = dataElementGroups[3] == "0"
        val username = if (dataElementGroups.size > 4) parseStringToNullIfEmpty(dataElementGroups[4]) else null
        val extension = if (dataElementGroups.size > 5) parseStringToNullIfEmpty(dataElementGroups[5]) else null

        return UserParameters(customerId, updVersion, areListedJobsBlocked, username, extension, segment)
    }

    protected open fun parseAccountInfo(segment: String, dataElementGroups: List<String>): AccountInfo {
        // this is parsing a Kontoverbindung. May extract a method for it.
        val accountDetails = getDataElements(dataElementGroups[1])
        val accountNumber = parseString(accountDetails[0])
        val subAccountAttribute = parseStringToNullIfEmpty(accountDetails[1])
        val bankCountryCode = parseInt(accountDetails[2])
        val bankCode = parseString(accountDetails[3])

        val iban = parseStringToNullIfEmpty(dataElementGroups[2])
        val customerId = parseString(dataElementGroups[3])
        val accountType = parseCodeEnum(dataElementGroups[4], AccountTypeCode.values()).type
        val currency = parseString(dataElementGroups[5])
        val accountHolderName1 = parseString(dataElementGroups[6])
        val accountHolderName2 = parseStringToNullIfEmpty(dataElementGroups[7])
        val productName = parseStringToNullIfEmpty(dataElementGroups[8])
        val limit = parseStringToNullIfEmpty(dataElementGroups[9]) // TODO: parse limit

        // TODO: parse allowed jobs
        // TODO: parse extension

        return AccountInfo(accountNumber, subAccountAttribute, bankCountryCode, bankCode, iban, customerId, accountType,
            currency, accountHolderName1, accountHolderName2, productName, limit, null, segment)
    }


    protected open fun parseBankDetails(dataElementsGroup: String): Kreditinstitutskennung {
        val detailsStrings = getDataElements(dataElementsGroup)

        return Kreditinstitutskennung(parseInt(detailsStrings[0]), parseString(detailsStrings[1]))
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
        val versionInt = parseInt(versionString)

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
        return splitIntoPartsAndUnmask(dataElementGroup, Separators.DataElementsSeparator)
    }

    /**
     * If separator symbols are used in data values, they are masked with '?'.
     * (e. g. 'https?://' should not be split into two data elements.)
     *
     * Don't split string at masked separators.
     *
     * After string is split, unmask separator
     *
     * Also binary data shouldn't be taken into account (TODO: really?).
     */
    protected open fun splitIntoPartsAndUnmask(dataString: String, separator: String): List<String> {
        val separatorMask = Separators.MaskingCharacter + separator
        val maskedSymbolsGuard = Separators.MaskingCharacter + "§"

        val maskedDataString = dataString.replace(separatorMask, maskedSymbolsGuard)

        val elements = maskedDataString.split(separator)

        return elements.map { it.replace(maskedSymbolsGuard, separator) }
    }

    protected open fun parseInt(string: String): Int {
        return parseString(string).toInt()
    }

    protected open fun parseStringToNullIfEmpty(string: String): String? {
        val parsedString = parseString(string)

        return if (parsedString.isEmpty()) null else parsedString
    }

    protected open fun parseString(string: String): String {

        return string
            // unmask mask data elements separator ('?:')
            .replace(Separators.MaskingCharacter + Separators.DataElementsSeparator, Separators.DataElementsSeparator)
            // masking character '?' is also masked, in his case with '??'
            .replace(Separators.MaskingCharacter + Separators.MaskingCharacter, Separators.MaskingCharacter)
    }

    protected open fun parseBoolean(dataElement: String): Boolean {
        if ("J" == dataElement) {
            return true
        }

        return false
    }

}
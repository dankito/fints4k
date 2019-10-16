package net.dankito.fints.response

import net.dankito.fints.messages.Separators
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Datum
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Uhrzeit
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.HbciVersion
import net.dankito.fints.messages.datenelemente.implementierte.ICodeEnum
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsverfahren
import net.dankito.fints.messages.datenelemente.implementierte.signatur.VersionDesSicherheitsverfahrens
import net.dankito.fints.messages.datenelemente.implementierte.tan.*
import net.dankito.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung
import net.dankito.fints.messages.datenelementgruppen.implementierte.signatur.Sicherheitsprofil
import net.dankito.fints.messages.segmente.id.MessageSegmentId
import net.dankito.fints.response.segments.*
import net.dankito.fints.transactions.IAccountTransactionsParser
import net.dankito.fints.transactions.Mt940AccountTransactionsParser
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


open class ResponseParser @JvmOverloads constructor(
    protected val mt940Parser: IAccountTransactionsParser = Mt940AccountTransactionsParser()
) {

    companion object {
        val BinaryDataHeaderPattern = Pattern.compile("@\\d+@")

        val EncryptionDataSegmentHeaderPattern = Pattern.compile("${MessageSegmentId.EncryptionData.id}:\\d{1,3}:\\d{1,3}\\+")

        val AllowedJobSegmentPattern = Pattern.compile("HI[A-Z]{3}S")

        val FeedbackParametersSeparator = "; "

        val SupportedTanProceduresForUserResponseCode = 3920

        private val log = LoggerFactory.getLogger(ResponseParser::class.java)
    }


    open fun parse(response: String): Response {
        try {
            val segments = splitIntoPartsAndUnmask(response, Separators.SegmentSeparator)

            val parsedSegments = segments.mapNotNull { parseSegment(it) }

            return Response(true, response, parsedSegments)
        } catch (e: Exception) {
            log.error("Could not parse response '$response'", e)

            return Response(true, response, exception = e)
        }
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

            InstituteSegmentId.MessageFeedback.id -> parseMessageFeedback(segment, dataElementGroups)
            InstituteSegmentId.SegmentFeedback.id -> parseSegmentFeedback(segment, dataElementGroups)

            InstituteSegmentId.Synchronization.id -> parseSynchronization(segment, dataElementGroups)
            InstituteSegmentId.BankParameters.id -> parseBankParameters(segment, dataElementGroups)
            InstituteSegmentId.SecurityMethods.id -> parseSecurityMethods(segment, dataElementGroups)
            InstituteSegmentId.CommunicationInfo.id -> parseCommunicationInfo(segment, dataElementGroups)

            InstituteSegmentId.UserParameters.id -> parseUserParameters(segment, dataElementGroups)
            InstituteSegmentId.AccountInfo.id -> parseAccountInfo(segment, dataElementGroups)

            InstituteSegmentId.TanInfo.id -> parseTanInfo(segment, segmentId, dataElementGroups)
            InstituteSegmentId.Tan.id -> parseTanResponse(segment, dataElementGroups)

            InstituteSegmentId.Balance.id -> parseBalanceSegment(segment, dataElementGroups)
            InstituteSegmentId.AccountTransactionsMt940.id -> parseMt940AccountTransactions(segment, dataElementGroups)

            else -> {
                if (AllowedJobSegmentPattern.matcher(segmentId).matches()) {
                    return parseAllowedJob(segment, segmentId, dataElementGroups)
                }

                UnparsedSegment(segment)
            }
        }
    }


    protected open fun parseMessageHeaderSegment(segment: String, dataElementGroups: List<String>): ReceivedMessageHeader {
        val messageSize = parseInt(dataElementGroups[1])
        val finTsVersion = parseInt(dataElementGroups[2])
        val dialogId = parseString(dataElementGroups[3])
        val messageNumber = parseInt(dataElementGroups[4])

        return ReceivedMessageHeader(messageSize, finTsVersion, dialogId, messageNumber, segment)
    }

    protected open fun parseMessageFeedback(segment: String, dataElementGroups: List<String>): MessageFeedback {
        val feedbacks = dataElementGroups.subList(1, dataElementGroups.size).map { parseFeedback(it) }

        return MessageFeedback(feedbacks, segment)
    }

    protected open fun parseSegmentFeedback(segment: String, dataElementGroups: List<String>): SegmentFeedback {
        val feedbacks = dataElementGroups.subList(1, dataElementGroups.size).map { parseFeedback(it) }

        return SegmentFeedback(feedbacks, segment)
    }

    protected open fun parseFeedback(dataElementGroup: String): Feedback {
        val dataElements = getDataElements(dataElementGroup)

        val responseCode = parseInt(dataElements[0])
        val referencedDataElement = parseStringToNullIfEmpty(dataElements[1])
        val message = parseString(dataElements[2])

        if (responseCode == SupportedTanProceduresForUserResponseCode) {
            val supportedProcedures = parseCodeEnum(dataElements.subList(3, dataElements.size), Sicherheitsfunktion.values())
            return SupportedTanProceduresForUserFeedback(supportedProcedures, message)
        }

        val parameter = if (dataElements.size > 3) dataElements.subList(3, dataElements.size).joinToString(FeedbackParametersSeparator) else null

        return Feedback(responseCode, message, referencedDataElement, parameter)
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

    protected open fun parseCommunicationInfo(segment: String, dataElementGroups: List<String>): CommunicationInfo {
        val bankDetails = parseBankDetails(dataElementGroups[1])
        val defaultLanguage = parseLanguage(dataElementGroups[2])
        val parameters = parseCommunicationParameters(dataElementGroups.subList(3, dataElementGroups.size))

        return CommunicationInfo(bankDetails, defaultLanguage, parameters, segment)
    }

    protected open fun parseCommunicationParameters(dataElementGroups: List<String>): List<CommunicationParameter> {
        return dataElementGroups.map { dataElementGroup ->
            val dataElements = getDataElements(dataElementGroup)

            CommunicationParameter(
                parseCodeEnum(dataElements[0], Kommunikationsdienst.values()),
                dataElements[1]
            )
        }
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

        val iban = parseStringToNullIfEmpty(dataElementGroups[2]) // optional
        val customerId = parseString(dataElementGroups[3])
        val accountType = parseNullableCodeEnum(dataElementGroups[4], AccountTypeCode.values())?.type
        val currency = parseStringToNullIfEmpty(dataElementGroups[5])
        val accountHolderName1 = parseString(dataElementGroups[6])
        val accountHolderName2 = if (dataElementGroups.size > 7) parseStringToNullIfEmpty(dataElementGroups[7]) else null
        val productName = if (dataElementGroups.size > 8) parseStringToNullIfEmpty(dataElementGroups[8]) else null
        val limit = if (dataElementGroups.size > 9) parseStringToNullIfEmpty(dataElementGroups[9]) else null // TODO: parse limit

        val allowedJobNames = if (dataElementGroups.size > 10) parseAllowedJobNames(dataElementGroups.subList(10, dataElementGroups.size - 1)) else listOf()
        val extension = if (dataElementGroups.size > 11) parseStringToNullIfEmpty(dataElementGroups[dataElementGroups.size - 1]) else null

        return AccountInfo(accountNumber, subAccountAttribute, bankCountryCode, bankCode, iban, customerId, accountType,
            currency, accountHolderName1, accountHolderName2, productName, limit, allowedJobNames, extension, segment)
    }

    protected open fun parseAllowedJobNames(dataElementGroups: List<String>): List<String> {

        return dataElementGroups.mapNotNull { parseAllowedJobName(it) }
    }

    protected open fun parseAllowedJobName(dataElementGroup: String): String? {
        val dataElements = getDataElements(dataElementGroup)

        if (dataElements.size > 0) {
            val jobName = parseString(dataElements[0])
            if (jobName.startsWith("HK")) { // filter out jobs not standardized by Deutsche Kreditwirtschaft (Verbandseigene Geschaeftsvorfaelle)
                return jobName
            }
        }

        return null
    }


    protected open fun parseAllowedJob(segment: String, segmentId: String, dataElementGroups: List<String>): SupportedJob {
        var jobName = segmentId.substring(0, 5) // cut off last 'S' (which stands for 'parameter')
        jobName = jobName.replaceFirst("HI", "HK")

        val maxCountJobs = parseInt(dataElementGroups[1])
        val minimumCountSignatures = parseInt(dataElementGroups[2])

        // Bei aelteren Version fehlt das Datenelement 'Sicherheitsklasse'. Ist fuer PIN/TAN eh zu ignorieren
        val securityClass = if (dataElementGroups.size > 3) parseNullableInt(dataElementGroups[3]) else null

        return SupportedJob(jobName, maxCountJobs, minimumCountSignatures, securityClass, segment)
    }


    protected open fun parseTanInfo(segment: String, segmentId: String, dataElementGroups: List<String>): TanInfo {
        val allowedJob = parseAllowedJob(segment, segmentId, dataElementGroups)

        return TanInfo(allowedJob, parseTwoStepTanProcedureParameters(dataElementGroups[4]))
    }

    protected open fun parseTwoStepTanProcedureParameters(tanProcedures: String): TwoStepTanProcedureParameters {
        val dataElements = getDataElements(tanProcedures)

        val oneStepProcedureAllowed = parseBoolean(dataElements[0])
        val moreThanOneTanDependentJobPerMessageAllowed = parseBoolean(dataElements[1])
        val jobHashValue = dataElements[2]

        val proceduresDataElements = dataElements.subList(3, dataElements.size)

        return TwoStepTanProcedureParameters(oneStepProcedureAllowed, moreThanOneTanDependentJobPerMessageAllowed,
            jobHashValue, mapToTanProcedureParameters(proceduresDataElements))
    }

    protected open fun mapToTanProcedureParameters(proceduresDataElements: List<String>): List<TanProcedureParameters> {
        // TODO: this throws an error for HITANS in version 4, but PSD2 needs HKTAN at least in version 6 anyway

        val parsedProceduresParameters = mutableListOf<TanProcedureParameters>()
        var remainingDataElements = proceduresDataElements

        while (remainingDataElements.size >= 20) { // parameters have at least 20 data elements, the last element is optional
            val dataElementForNextProcedure = if (remainingDataElements.size >= 21) remainingDataElements.subList(0, 21)
                                            else remainingDataElements.subList(0, 20)

            val procedureParameters = mapToSingleTanProcedureParameters(dataElementForNextProcedure)
            parsedProceduresParameters.add(procedureParameters)

            if (procedureParameters.countSupportedActiveTanMedia != null) remainingDataElements = remainingDataElements.subList(21, remainingDataElements.size)
            else remainingDataElements = remainingDataElements.subList(20, remainingDataElements.size)
        }

        return parsedProceduresParameters
    }

    protected open fun mapToSingleTanProcedureParameters(procedureDataElements: List<String>): TanProcedureParameters {

        return TanProcedureParameters(
            parseCodeEnum(procedureDataElements[0], Sicherheitsfunktion.values()),
            parseCodeEnum(procedureDataElements[1], TanProcess.values()),
            parseString(procedureDataElements[2]),
            tryToParseZkaTanProcedure(procedureDataElements[3]),
            parseStringToNullIfEmpty(procedureDataElements[4]),
            parseString(procedureDataElements[5]),
            parseInt(procedureDataElements[6]),
            parseCodeEnum(procedureDataElements[7], AllowedTanFormat.values()),
            parseString(procedureDataElements[8]),
            parseInt(procedureDataElements[9]),
            parseBoolean(procedureDataElements[10]),
            parseCodeEnum(procedureDataElements[11], TanZeitUndDialogbezug.values()),
            parseBoolean(procedureDataElements[12]),
            parseCodeEnum(procedureDataElements[13], SmsAbbuchungskontoErforderlich.values()),
            parseCodeEnum(procedureDataElements[14], AuftraggeberkontoErforderlich.values()),
            parseBoolean(procedureDataElements[15]),
            parseBoolean(procedureDataElements[16]),
            parseCodeEnum(procedureDataElements[17], Initialisierungsmodus.values()),
            parseCodeEnum(procedureDataElements[18], BezeichnungDesTanMediumsErforderlich.values()),
            parseBoolean(procedureDataElements[19]),
            if (procedureDataElements.size > 20) parseNullableInt(procedureDataElements[20]) else null
        )
    }

    protected open fun tryToParseZkaTanProcedure(mayZkaTanProcedure: String): ZkaTanProcedure? {
        try {
            return ZkaTanProcedure.valueOf(mayZkaTanProcedure)
        } catch (ignored: Exception) { }

        return null
    }


    protected open fun parseTanResponse(segment: String, dataElementGroups: List<String>): TanResponse {
        val binaryJobHashValue = if (dataElementGroups.size > 2) parseStringToNullIfEmpty(dataElementGroups[2]) else null
        val binaryChallengeHHD_UC = if (dataElementGroups.size > 5) parseStringToNullIfEmpty(dataElementGroups[5]) else null

        return TanResponse(
            parseCodeEnum(dataElementGroups[1], TanProcess.values()),
            binaryJobHashValue?.let { extractBinaryData(it) },
            if (dataElementGroups.size > 3) parseStringToNullIfEmpty(dataElementGroups[3]) else null,
            if (dataElementGroups.size > 4) parseStringToNullIfEmpty(dataElementGroups[4]) else null,
            binaryChallengeHHD_UC?.let { extractBinaryData(it) },
            if (dataElementGroups.size > 6) parseNullableDateTime(dataElementGroups[6]) else null,
            if (dataElementGroups.size > 7) parseStringToNullIfEmpty(dataElementGroups[7]) else null,
            segment
        )
    }


    protected open fun parseBalanceSegment(segment: String, dataElementGroups: List<String>): BalanceSegment {
        // dataElementGroups[1] is account details

        val balance = parseBalance(dataElementGroups[4])
        val balanceOfPreBookedTransactions = parseBalance(dataElementGroups[5])

        return BalanceSegment(
            balance.amount,
            parseString(dataElementGroups[3]),
            balance.date,
            parseString(dataElementGroups[2]),
            if (balanceOfPreBookedTransactions.amount.equals(BigDecimal.ZERO)) null else balanceOfPreBookedTransactions.amount,
            segment
        )
    }

    protected open fun parseBalance(dataElementGroup: String): Balance {
        val dataElements = getDataElements(dataElementGroup)

        val isCredit = parseString(dataElements[0]) == "C"

        var dateIndex = 2
        var date: Date? = parseNullableDate(dataElements[dateIndex]) // in older versions dateElements[2] was the currency
        if (date == null) {
            date = parseDate(dataElements[++dateIndex])
        }

        return Balance(
            parseAmount(dataElements[1], isCredit),
            date,
            if (dataElements.size > dateIndex + 1) parseTime(dataElements[dateIndex + 1]) else null
        )
    }


    protected open fun parseMt940AccountTransactions(segment: String, dataElementGroups: List<String>): ReceivedAccountTransactions {
        val bookedTransactionsString = extractBinaryData(dataElementGroups[1])

        // TODO: implement parsing MT942
        val unbookedTransactionsString = if (dataElementGroups.size > 2) extractBinaryData(dataElementGroups[2]) else null

        return ReceivedAccountTransactions(mt940Parser.parseTransactions(bookedTransactionsString), listOf(), segment)
    }


    protected open fun parseBankDetails(dataElementsGroup: String): Kreditinstitutskennung {
        val detailsStrings = getDataElements(dataElementsGroup)

        return Kreditinstitutskennung(parseInt(detailsStrings[0]), parseString(detailsStrings[1]))
    }

    protected open fun parseLanguages(dataElementsGroup: String): List<Dialogsprache> {
        return getDataElements(dataElementsGroup).map { parseLanguage(it) }
    }

    protected open fun parseLanguage(dataElementsGroup: String): Dialogsprache {
        return parseCodeEnum(dataElementsGroup, Dialogsprache.values())
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

    protected open fun <T : ICodeEnum> parseNullableCodeEnum(code: String, allValues: Array<T>): T? {
        try {
            return parseCodeEnum(code, allValues)
        } catch (ignored: Exception) { }

        return null
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
        val binaryDataRanges = mutableListOf<IntRange>()
        val binaryDataMatcher = BinaryDataHeaderPattern.matcher(dataString)

        while (binaryDataMatcher.find()) {
            if (isEncryptionDataSegment(dataString, binaryDataMatcher) == false) {
                val startIndex = binaryDataMatcher.end()
                val length = binaryDataMatcher.group().replace("@", "").toInt()

                binaryDataRanges.add(IntRange(startIndex, startIndex + length - 1))
            }
        }

        val separatorIndices = allIndicesOf(dataString, separator)
            .filter { isCharacterMasked(it, dataString) == false }
            .filter { isInRange(it, binaryDataRanges) == false }

        var startIndex = 0
        val elements = separatorIndices.map { endIndex ->
            val element = dataString.substring(startIndex, endIndex)
            startIndex = endIndex + 1
            element
        }.toMutableList()

        if (startIndex < dataString.length) {
            elements.add(dataString.substring(startIndex))
        }

        return elements.map { it.replace(Separators.MaskingCharacter + separator, separator) }
    }

    protected open fun isEncryptionDataSegment(dataString: String, binaryDataMatcher: Matcher): Boolean {
        val binaryDataHeaderStartIndex = binaryDataMatcher.start()

        if (binaryDataHeaderStartIndex > 15) {
            val encryptionDataSegmentMatcher = EncryptionDataSegmentHeaderPattern.matcher(dataString)

            if (encryptionDataSegmentMatcher.find(binaryDataHeaderStartIndex - 15)) {
                return encryptionDataSegmentMatcher.start() < binaryDataHeaderStartIndex
            }
        }

        return false
    }

    protected open fun isCharacterMasked(characterIndex: Int, wholeString: String): Boolean {
        if (characterIndex > 0) {
            val previousChar = wholeString[characterIndex - 1]

            return previousChar.toString() == Separators.MaskingCharacter
        }

        return false
    }

    protected open fun isInRange(index: Int, ranges: List<IntRange>): Boolean {
        for (range in ranges) {
            if (range.contains(index)) {
                return true
            }
        }

        return false
    }

    protected open fun allIndicesOf(string: String, toFind: String): List<Int> {
        val indices = mutableListOf<Int>()
        var index = -1

        do {
            index = string.indexOf(toFind, index + 1)

            if (index > -1) {
                indices.add(index)
            }
        } while (index > -1)

        return indices
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

    protected open fun parseInt(string: String): Int {
        return parseString(string).toInt()
    }

    protected open fun parseNullableInt(mayInt: String): Int? {
        try {
            return parseInt(mayInt)
        } catch (ignored: Exception) { }

        return null
    }

    @JvmOverloads
    protected open fun parseAmount(amountString: String, isPositive: Boolean = true): BigDecimal {
        val adjustedAmountString = amountString.replace(',', '.') // Hbci amount format uses comma instead dot as decimal separator

        val amount = adjustedAmountString.toBigDecimal()

        if (isPositive == false) {
            return amount.negate()
        }

        return amount
    }

    protected open fun parseNullableDateTime(dataElementGroup: String): Date? {
        val dataElements = getDataElements(dataElementGroup)

        if (dataElements.size >= 2) {
            parseNullableDate(dataElements[0])?.let { date ->
                parseNullableTime(dataElements[1])?.let { time ->
                    return Date(date.time + time.time)
                }
            }
        }

        return null
    }

    protected open fun parseDate(dateString: String): Date {
        return Datum.HbciDateFormat.parse(dateString)
    }

    protected open fun parseNullableDate(dateString: String): Date? {
        try {
            return parseDate(dateString)
        } catch (ignored: Exception) { }

        return null
    }

    protected open fun parseTime(timeString: String): Date {
        return Uhrzeit.HbciTimeFormat.parse(timeString)
    }

    protected open fun parseNullableTime(timeString: String): Date? {
        try {
            return parseTime(timeString)
        } catch (ignored: Exception) { }

        return null
    }

    protected open fun extractBinaryData(binaryData: String): String {
        if (binaryData.startsWith('@')) {
            val headerEndIndex = binaryData.indexOf('@', 2)

            if (headerEndIndex > -1) {
                return binaryData.substring(headerEndIndex + 1)
            }
        }

        return binaryData
    }

}
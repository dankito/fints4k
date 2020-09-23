package net.dankito.banking.fints.response

import net.dankito.banking.fints.messages.Separators
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Datum
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Uhrzeit
import net.dankito.banking.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.banking.fints.messages.datenelemente.implementierte.HbciVersion
import net.dankito.banking.fints.messages.datenelemente.implementierte.ICodeEnum
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Sicherheitsverfahren
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.VersionDesSicherheitsverfahrens
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.*
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.account.KontoverbindungInternational
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.signatur.Sicherheitsprofil
import net.dankito.banking.fints.messages.segmente.id.MessageSegmentId
import net.dankito.banking.fints.model.Amount
import net.dankito.banking.fints.model.CreditCardTransaction
import net.dankito.banking.fints.model.Money
import net.dankito.banking.fints.response.segments.*
import net.dankito.banking.fints.util.MessageUtils
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.getInnerExceptionMessage
import net.dankito.utils.multiplatform.log.LoggerFactory


open class ResponseParser(
    protected val messageUtils: MessageUtils = MessageUtils()
) {

    companion object {
        val JobParametersSegmentRegex = Regex("[H|D]I[A-Z]{3}S")

        const val FeedbackParametersSeparator = "; "

        const val AufsetzpunktResponseCode = 3040

        const val SupportedTanMethodsForUserResponseCode = 3920


        private val log = LoggerFactory.getLogger(ResponseParser::class)
    }


    open fun parse(response: String): BankResponse {
        try {
            val segments = splitIntoPartsAndUnmask(response, Separators.SegmentSeparatorChar).toMutableList()

            extractSegmentEmbeddedInEncryptedData(segments)

            val parsedSegments = segments.mapNotNull { parseSegment(it) }

            return BankResponse(true, response, parsedSegments)
        } catch (e: Exception) {
            log.error(e) { "Could not parse response '$response'" }

            return BankResponse(true, response, errorMessage = e.getInnerExceptionMessage())
        }
    }

    protected open fun extractSegmentEmbeddedInEncryptedData(elements: MutableList<String>) {
        ArrayList(elements).forEachIndexed { index, element ->
            if (element?.startsWith(MessageSegmentId.EncryptionData.id) == true) {
                val embeddedSegmentBinaryDataStartIndex = element.indexOf(Separators.BinaryDataSeparatorChar)

                if (embeddedSegmentBinaryDataStartIndex > 0) {
                    val inEncryptedDataSegmentEmbeddedSegment = extractBinaryData(element.substring(embeddedSegmentBinaryDataStartIndex))

                    elements.add(index + 1, inEncryptedDataSegmentEmbeddedSegment)
                    elements[index] = element.substring(0, embeddedSegmentBinaryDataStartIndex)
                }
            }
        }
    }


    protected open fun parseSegment(segment: String): ReceivedSegment? {
        try {
            if (segment.isNotEmpty()) { // filter out empty lines
                val dataElementGroups = splitIntoPartsAndUnmask(segment, Separators.DataElementGroupsSeparatorChar)

                val segmentId = segment.substring(0, segment.indexOf(Separators.DataElementsSeparator))

                return parseSegment(segment, segmentId, dataElementGroups)
            }
        } catch (e: Exception) {
            log.error(e) { "Could not parse segment '$segment'" } // TODO: what to do here, how to inform user?
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
            InstituteSegmentId.SepaAccountInfo.id -> parseSepaAccountInfo(segment, dataElementGroups)
            InstituteSegmentId.SepaAccountInfoParameters.id -> parseSepaAccountInfoParameters(segment, segmentId, dataElementGroups)

            InstituteSegmentId.PinInfo.id -> parsePinInfo(segment, segmentId, dataElementGroups)
            InstituteSegmentId.TanInfo.id -> parseTanInfo(segment, segmentId, dataElementGroups)
            InstituteSegmentId.Tan.id -> parseTanResponse(segment, dataElementGroups)
            InstituteSegmentId.TanMediaList.id -> parseTanMediaList(segment, dataElementGroups)
            InstituteSegmentId.ChangeTanMediaParameters.id -> parseChangeTanMediaParameters(segment, segmentId, dataElementGroups)

            InstituteSegmentId.Balance.id -> parseBalanceSegment(segment, dataElementGroups)

            InstituteSegmentId.AccountTransactionsMt940.id -> parseMt940AccountTransactions(segment, dataElementGroups)
            InstituteSegmentId.AccountTransactionsMt940Parameters.id -> parseMt940AccountTransactionsParameters(segment, segmentId, dataElementGroups)

            InstituteSegmentId.CreditCardTransactions.id -> parseCreditCardTransactions(segment, dataElementGroups)

            else -> {
                if (JobParametersSegmentRegex.matches(segmentId)) {
                    return parseJobParameters(segment, segmentId, dataElementGroups)
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

        if (responseCode == SupportedTanMethodsForUserResponseCode) {
            val supportedMethods = parseCodeEnum(dataElements.subList(3, dataElements.size), Sicherheitsfunktion.values())
            return SupportedTanMethodsForUserFeedback(supportedMethods, message)
        }
        else if (responseCode == AufsetzpunktResponseCode) {
            return AufsetzpunktFeedback(parseString(dataElements[3]), message)
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

        val maxMessageSize = if (dataElementGroups.size > 7) parseIntToNullIfEmpty(dataElementGroups[7]) else null
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

        val isExtensionSet = dataElementGroups.size > 11 && dataElementGroups.last().endsWith('}')
        val allowedJobNames = if (dataElementGroups.size > 10) parseAllowedJobNames(dataElementGroups.subList(10, if (isExtensionSet) dataElementGroups.size - 1 else dataElementGroups.size)) else listOf()
        val extension = if (isExtensionSet) parseStringToNullIfEmpty(dataElementGroups[dataElementGroups.size - 1]) else null

        return AccountInfo(accountNumber, subAccountAttribute, bankCountryCode, bankCode, iban, customerId, accountType,
            currency, accountHolderName1, accountHolderName2, productName, limit, allowedJobNames, extension, segment)
    }

    protected open fun parseAllowedJobNames(dataElementGroups: List<String>): List<String> {

        return dataElementGroups.filterNot { it.isEmpty() }.mapNotNull { parseAllowedJobName(it) }
    }

    protected open fun parseAllowedJobName(dataElementGroup: String): String? {
        val dataElements = getDataElementsThatContainsNoMaskSeparators(dataElementGroup)

        if (dataElements.size > 0) {
            val jobName = parseString(dataElements[0])
            return jobName
        }

        return null
    }

    protected open fun parseSepaAccountInfo(segment: String, dataElementGroups: List<String>): SepaAccountInfo {
        val accountDataElements = getDataElements(dataElementGroups[1])

        return SepaAccountInfo(
            KontoverbindungZvInternational(
                parseBoolean(accountDataElements[0]),
                parseStringToNullIfEmpty(accountDataElements[1]),
                parseStringToNullIfEmpty(accountDataElements[2]),
                parseString(accountDataElements[3]),
                parseStringToNullIfEmpty(accountDataElements[4]),
                parseBankDetails(accountDataElements[5], accountDataElements[6])
            ),
            segment
        )
    }

    protected open fun parseSepaAccountInfoParameters(segment: String, segmentId: String, dataElementGroups: List<String>): SepaAccountInfoParameters {
        val jobParameters = parseJobParameters(segment, segmentId, dataElementGroups)
        val segmentVersion = jobParameters.segmentVersion

        val parametersDataElements = getDataElements(dataElementGroups[4])
        val supportedSepaFormatsBeginIndex = if (segmentVersion == 1) 3 else if (segmentVersion == 2) 4 else 5

        return SepaAccountInfoParameters(
            jobParameters,
            parseBoolean(parametersDataElements[0]),
            parseBoolean(parametersDataElements[1]),
            parseBoolean(parametersDataElements[2]),
            if (segmentVersion >= 2) parseBoolean(parametersDataElements[3]) else false,
            if (segmentVersion >= 3) parseInt(parametersDataElements[4]) else SepaAccountInfoParameters.CountReservedUsageLengthNotSet,
            parametersDataElements.subList(supportedSepaFormatsBeginIndex, parametersDataElements.size)
        )
    }


    protected open fun parseJobParameters(segment: String, segmentId: String, dataElementGroups: List<String>): JobParameters {
        var jobName = segmentId.substring(0, 5) // cut off last 'S' (which stands for 'parameter')
        jobName = jobName.replaceFirst("HI", "HK").replaceFirst("DI", "DK")

        val maxCountJobs = parseInt(dataElementGroups[1])
        val minimumCountSignatures = parseInt(dataElementGroups[2])

        // Bei aelteren Version fehlt das Datenelement 'Sicherheitsklasse'. Ist fuer PIN/TAN eh zu ignorieren
        val securityClass = if (dataElementGroups.size > 3) parseIntToNullIfEmpty(dataElementGroups[3]) else null

        return JobParameters(jobName, maxCountJobs, minimumCountSignatures, securityClass, segment)
    }


    protected open fun parsePinInfo(segment: String, segmentId: String, dataElementGroups: List<String>): PinInfo {
        val jobParameters = parseJobParameters(segment, segmentId, dataElementGroups)

        val dataElements = getDataElements(dataElementGroups[4])

        val minPinLength = parseIntToNullIfEmpty(dataElements[0])
        val maxPinLength = parseIntToNullIfEmpty(dataElements[1])
        val minTanLength = parseIntToNullIfEmpty(dataElements[2])
        val userIdHint = parseStringToNullIfEmpty(dataElements[3])
        val customerIdHint = parseStringToNullIfEmpty(dataElements[4])

        return PinInfo(jobParameters, minPinLength, maxPinLength, minTanLength, userIdHint, customerIdHint,
            parseJobTanConfigurations(dataElements.subList(5, dataElements.size)))
    }

    protected open fun parseJobTanConfigurations(dataElementGroups: List<String>): List<JobTanConfiguration> {
        val jobTanConfigurations = mutableListOf<JobTanConfiguration>()

        for (i in dataElementGroups.indices step 2) {
            jobTanConfigurations.add(
                JobTanConfiguration(
                    parseString(dataElementGroups[i]),
                    parseBoolean(dataElementGroups[i + 1])
                )
            )
        }

        return jobTanConfigurations
    }


    protected open fun parseTanInfo(segment: String, segmentId: String, dataElementGroups: List<String>): TanInfo? {
        val jobParameters = parseJobParameters(segment, segmentId, dataElementGroups)

        if (jobParameters.segmentVersion < 6) { // Versions 4 and 5 have a different, outdated format, we're not able to parse this
            return null
        }

        return TanInfo(jobParameters, parseTwoStepTanProcedureParameters(dataElementGroups[4]))
    }

    protected open fun parseTwoStepTanProcedureParameters(tanProcedures: String): TwoStepTanProcedureParameters {
        val dataElements = getDataElements(tanProcedures)

        val oneStepProcedureAllowed = parseBoolean(dataElements[0])
        val moreThanOneTanDependentJobPerMessageAllowed = parseBoolean(dataElements[1])
        val jobHashValue = dataElements[2]

        val proceduresDataElements = dataElements.subList(3, dataElements.size)

        return TwoStepTanProcedureParameters(oneStepProcedureAllowed, moreThanOneTanDependentJobPerMessageAllowed,
            jobHashValue, mapToTanMethodParameters(proceduresDataElements))
    }

    protected open fun mapToTanMethodParameters(methodsDataElements: List<String>): List<TanMethodParameters> {
        // TODO: this throws an error for HITANS in version 4, but PSD2 needs HKTAN at least in version 6 anyway

        val parsedMethodsParameters = mutableListOf<TanMethodParameters>()
        var remainingDataElements = methodsDataElements

        while (remainingDataElements.size >= 20) { // parameters have at least 20 data elements, the last element is optional
            val dataElementForNextMethod = if (remainingDataElements.size >= 21) remainingDataElements.subList(0, 21)
                                            else remainingDataElements.subList(0, 20)

            val methodParameters = mapToSingleTanMethodParameters(dataElementForNextMethod)
            parsedMethodsParameters.add(methodParameters)

            val has21ElementsParsed = methodParameters.countSupportedActiveTanMedia != null ||
                    (dataElementForNextMethod.size >= 21 && dataElementForNextMethod[20].isBlank())

            if (has21ElementsParsed) remainingDataElements = remainingDataElements.subList(21, remainingDataElements.size)
            else remainingDataElements = remainingDataElements.subList(20, remainingDataElements.size)
        }

        return parsedMethodsParameters
    }

    protected open fun mapToSingleTanMethodParameters(methodDataElements: List<String>): TanMethodParameters {

        return TanMethodParameters(
            parseCodeEnum(methodDataElements[0], Sicherheitsfunktion.values()),
            parseCodeEnum(methodDataElements[1], TanProcess.values()),
            parseString(methodDataElements[2]),
            tryToParseZkaTanMethod(methodDataElements[3]),
            parseStringToNullIfEmpty(methodDataElements[4]),
            parseString(methodDataElements[5]),
            parseInt(methodDataElements[6]),
            parseCodeEnum(methodDataElements[7], AllowedTanFormat.values()),
            parseString(methodDataElements[8]),
            parseInt(methodDataElements[9]),
            // for HITANS 4 and 5 here is another "Anzahl unterstützter aktiver TAN-Listen" Integer element
            parseBoolean(methodDataElements[10]),
            parseCodeEnum(methodDataElements[11], TanZeitUndDialogbezug.values()),
            // for HITANS 4 and 5 here is another "TAN-Listennummer erforderlich" code element
            parseBoolean(methodDataElements[12]),
            tryToParseSmsAbbuchungskontoErforderlich(methodDataElements[13]),
            tryToParseAuftraggeberkontoErforderlich(methodDataElements[14]),
            parseBoolean(methodDataElements[15]),
            parseBoolean(methodDataElements[16]),
            parseCodeEnum(methodDataElements[17], Initialisierungsmodus.values()),
            parseCodeEnum(methodDataElements[18], BezeichnungDesTanMediumsErforderlich.values()),
            parseBoolean(methodDataElements[19]),
            if (methodDataElements.size > 20) parseNullableInt(methodDataElements[20]) else null
        )
    }

    protected open fun tryToParseZkaTanMethod(mayZkaTanMethod: String): ZkaTanMethod? {
        if (mayZkaTanMethod.isBlank()) {
            return null
        }

        try {
            val lowerCaseMayZkaTanMethod = mayZkaTanMethod.toLowerCase()

            if (lowerCaseMayZkaTanMethod == "mobiletan" || lowerCaseMayZkaTanMethod == "mtan") {
                return ZkaTanMethod.mobileTAN
            }

            if (lowerCaseMayZkaTanMethod == "apptan" || lowerCaseMayZkaTanMethod == "phototan") {
                return ZkaTanMethod.appTAN
            }

            // TODO: what about these values, all returned by banks in anonymous dialog initialization:
            //  BestSign, HHDUSB1, Secoder_UC, ZkaTANMode, photoTAN, QRTAN, 1822TAN+

            return ZkaTanMethod.valueOf(mayZkaTanMethod)
        } catch (ignored: Exception) { }

        return null
    }

    protected open fun tryToParseSmsAbbuchungskontoErforderlich(smsAbbuchungskontoErforderlichString: String): SmsAbbuchungskontoErforderlich {
        try {
            return parseCodeEnum(smsAbbuchungskontoErforderlichString, SmsAbbuchungskontoErforderlich.values())
        } catch (e: Exception) {
            if (isEncodedBooleanValue(smsAbbuchungskontoErforderlichString) == false) {
                log.error(e) { "Could not parse '$smsAbbuchungskontoErforderlichString' to SmsAbbuchungskontoErforderlich" }
            }
        }

        // Bankhaus Neelmeyer and Oldenburgische Landesbank encode SmsAbbuchungskontoErforderlich with boolean values (with is wrong according to FinTS standard)
        return tryToParseEnumAsBoolean(smsAbbuchungskontoErforderlichString,
            SmsAbbuchungskontoErforderlich.SmsAbbuchungskontoMussAngegebenWerden,
            SmsAbbuchungskontoErforderlich.SmsAbbuchungskontoDarfNichtAngegebenWerden)
    }

    protected open fun tryToParseAuftraggeberkontoErforderlich(auftraggeberkontoErforderlichString: String): AuftraggeberkontoErforderlich {
        try {
            return parseCodeEnum(auftraggeberkontoErforderlichString, AuftraggeberkontoErforderlich.values())
        } catch (e: Exception) {
            if (isEncodedBooleanValue(auftraggeberkontoErforderlichString) == false) {
                log.error(e) { "Could not parse '$auftraggeberkontoErforderlichString' to AuftraggeberkontoErforderlich" }
            }
        }

        // Bankhaus Neelmeyer and Oldenburgische Landesbank encode AuftraggeberkontoErforderlich with boolean values (with is wrong according to FinTS standard)
        return tryToParseEnumAsBoolean(auftraggeberkontoErforderlichString,
            AuftraggeberkontoErforderlich.AuftraggeberkontoMussAngegebenWerdenWennImGeschaeftsvorfallEnthalten,
            AuftraggeberkontoErforderlich.AuftraggeberkontoDarfNichtAngegebenWerden)
    }

    protected open fun isEncodedBooleanValue(value: String): Boolean {
        return value == "N" || value == "J"
    }

    protected open fun <T : Enum<T>> tryToParseEnumAsBoolean(enumString: String, valueForTrue: T, valueForFalse: T): T {
        try {
            val bool = parseBoolean(enumString)

            return if (bool) {
                valueForTrue
            }
            else {
                valueForFalse
            }
        } catch (e: Exception) { }

        return valueForFalse
    }


    protected open fun parseTanResponse(segment: String, dataElementGroups: List<String>): TanResponse {
        val binaryJobHashValue = if (dataElementGroups.size > 2) parseStringToNullIfEmpty(dataElementGroups[2]) else null
        val binaryChallengeHHD_UC = if (dataElementGroups.size > 5 && dataElementGroups[5].isNotEmpty()) dataElementGroups[5] else null

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

    protected open fun parseTanMediaList(segment: String, dataElementGroups: List<String>): TanMediaList {
        val usageOption = parseCodeEnum(dataElementGroups[1], TanEinsatzOption.values())
        val segmentVersion = parseInt(getDataElements(dataElementGroups[0])[2])

        return TanMediaList(usageOption,
            parseTanMedia(segmentVersion, dataElementGroups.subList(2, dataElementGroups.size)),
            segment)
    }

    protected open fun parseTanMedia(hitabVersion: Int, dataElementGroups: List<String>): List<TanMedium> {
        return dataElementGroups.map { getDataElements(it) }.map { parseTanMedium(hitabVersion, it) }
    }

    protected open fun parseTanMedium(hitabVersion: Int, dataElements: List<String>): TanMedium {
        val mediumClassCode = dataElements[0]
        val mediumClass = parseCodeEnum(mediumClassCode, TanMediumKlasse.values())
        if (mediumClass.supportedHkTabVersions.contains(hitabVersion) == false) {
            throw UnsupportedOperationException("$mediumClassCode is not a valid medium class for HITAB version $hitabVersion. " +
                    "Supported values are: " + TanMediumKlasse.values().filter { it.supportedHkTabVersions.contains(hitabVersion) }.map { it.code })
        }

        val status = parseCodeEnum(dataElements[1], TanMediumStatus.values())

        // TODO: may also parse 'Letzte Benutzung' (second last element) and 'Freigeschaltet am' (last element)

        val remainingDataElements = dataElements.subList(2, dataElements.size)

        return when (mediumClass) {
            TanMediumKlasse.TanGenerator -> parseTanGeneratorTanMedium(mediumClass, status, hitabVersion, remainingDataElements)
            TanMediumKlasse.MobiltelefonMitMobileTan -> parseMobilePhoneTanMedium(mediumClass, status, hitabVersion, remainingDataElements)
            else -> TanMedium(mediumClass, status, "Unbekannte TAN Medium Klasse")
        }
    }

    protected open fun parseTanGeneratorTanMedium(mediumClass: TanMediumKlasse, status: TanMediumStatus,
                                                  hitabVersion: Int, dataElements: List<String>): TanGeneratorTanMedium {

        val cardType = if (hitabVersion < 2) null else parseNullableInt(dataElements[2])
        // TODO: may also parse account info
        val validFrom = if (hitabVersion < 2) null else parseNullableDate(dataElements[8])
        val validTo = if (hitabVersion < 2) null else parseNullableDate(dataElements[9])
        val mediumName = if (hitabVersion < 2) null else parseStringToNullIfEmpty(dataElements[10])

        return TanGeneratorTanMedium(mediumClass, status, parseString(dataElements[0]), parseStringToNullIfEmpty(dataElements[1]),
            cardType, validFrom, validTo, mediumName)
    }

    protected open fun parseMobilePhoneTanMedium(mediumClass: TanMediumKlasse, status: TanMediumStatus,
                                                  hitabVersion: Int, dataElements: List<String>): MobilePhoneTanMedium {

        val mediumName = parseString(dataElements[10])
        val concealedPhoneNumber = if (hitabVersion < 2) null else parseStringToNullIfEmpty(dataElements[11])
        val phoneNumber = if (hitabVersion < 2) null else parseStringToNullIfEmpty(dataElements[12])
        val smsDebitAccount: KontoverbindungInternational? = null // TODO: may parse 13th data element to KontoverbindungInternational

        return MobilePhoneTanMedium(mediumClass, status, mediumName, concealedPhoneNumber, phoneNumber, smsDebitAccount)
    }


    protected open fun parseChangeTanMediaParameters(segment: String, segmentId: String, dataElementGroups: List<String>): ChangeTanMediaParameters {
        val jobParameters = parseJobParameters(segment, segmentId, dataElementGroups)

        val hiTausSegmentVersion = parseInt(getDataElements(dataElementGroups[0])[2])
        val changeTanGeneratorParameters = getDataElements(dataElementGroups[4])

        val enteringCardTypeRequired = if (hiTausSegmentVersion < 2) false else parseBoolean(changeTanGeneratorParameters[3])
        val accountInfoRequired = if (hiTausSegmentVersion < 3) false else parseBoolean(changeTanGeneratorParameters[4])

        val remainingParameters = when (hiTausSegmentVersion) {
            1 -> listOf()
            2 -> changeTanGeneratorParameters.subList(4, changeTanGeneratorParameters.size)
            else -> changeTanGeneratorParameters.subList(5, changeTanGeneratorParameters.size)
        }
        val allowedCardTypes = remainingParameters.map { parseInt(it) }

        return ChangeTanMediaParameters(jobParameters, parseBoolean(changeTanGeneratorParameters[0]),
            parseBoolean(changeTanGeneratorParameters[1]), parseBoolean(changeTanGeneratorParameters[2]),
            enteringCardTypeRequired, accountInfoRequired, allowedCardTypes)
    }


    protected open fun parseBalanceSegment(segment: String, dataElementGroups: List<String>): BalanceSegment {
        // dataElementGroups[1] is account details

        val balance = parseBalance(dataElementGroups[4])
        val balanceOfPreBookedTransactions = if (dataElementGroups.size > 5) parseBalanceToNullIfZeroOrNotSet(dataElementGroups[5]) else null

        return BalanceSegment(
            balance.amount,
            parseString(dataElementGroups[3]),
            balance.date,
            parseString(dataElementGroups[2]),
            balanceOfPreBookedTransactions?.amount,
            segment
        )
    }

    protected open fun parseBalanceToNullIfZeroOrNotSet(dataElementGroup: String): Balance? {
        if (dataElementGroup.isEmpty()) {
            return null
        }

        val parsedBalance = parseBalance(dataElementGroup)

        if (Amount.Zero.equals(parsedBalance.amount)) {
            return null
        }

        return parsedBalance
    }

    protected open fun parseBalance(dataElementGroup: String): Balance {
        val dataElements = getDataElements(dataElementGroup)

        val isCredit = parseIsCredit(dataElements[0])
        var currency: String? = null

        var dateIndex = 2
        var date: Date? = parseNullableDate(dataElements[dateIndex]) // in older versions dateElements[2] was the currency
        if (date == null) {
            currency = parseString(dataElements[dateIndex])
            date = parseDate(dataElements[++dateIndex])
        }

        return Balance(
            parseAmount(dataElements[1], isCredit),
            currency,
            date,
            if (dataElements.size > dateIndex + 1) parseTime(dataElements[dateIndex + 1]) else null
        )
    }

    protected open fun parseIsCredit(isCredit: String): Boolean {
        return parseString(isCredit) == "C"
    }


    protected open fun parseMt940AccountTransactions(segment: String, dataElementGroups: List<String>): ReceivedAccountTransactions {
        val bookedTransactionsString = extractBinaryData(dataElementGroups[1])

        val unbookedTransactionsString = if (dataElementGroups.size > 2) extractBinaryData(dataElementGroups[2]) else null

        return ReceivedAccountTransactions(bookedTransactionsString, unbookedTransactionsString, segment)
    }

    protected open fun parseMt940AccountTransactionsParameters(segment: String, segmentId: String, dataElementGroups: List<String>): RetrieveAccountTransactionsInMt940Parameters {
        val jobParameters = parseJobParameters(segment, segmentId, dataElementGroups)

        val transactionsParameterIndex = if (jobParameters.segmentVersion >= 6) 4 else 3
        val dataElements = getDataElements(dataElementGroups[transactionsParameterIndex])

        val countDaysForWhichTransactionsAreKept = parseInt(dataElements[0])
        val settingCountEntriesAllowed = parseBoolean(dataElements[1])
        val settingAllAccountAllowed = if (dataElements.size > 2) parseBoolean(dataElements[2]) else false

        return RetrieveAccountTransactionsInMt940Parameters(jobParameters, countDaysForWhichTransactionsAreKept, settingCountEntriesAllowed, settingAllAccountAllowed)
    }


    protected open fun parseCreditCardTransactions(segment: String, dataElementGroups: List<String>): ReceivedCreditCardTransactionsAndBalance {
        val balance = parseBalance(dataElementGroups[3])
        val transactionsDataElementGroups = dataElementGroups.subList(6, dataElementGroups.size)

        return ReceivedCreditCardTransactionsAndBalance(
            balance,
            transactionsDataElementGroups.map { mapCreditCardTransaction(it) },
            segment
        )
    }

    protected open fun mapCreditCardTransaction(transactionDataElementGroup: String): CreditCardTransaction {
        val dataElements = getDataElements(transactionDataElementGroup)

        val bookingDate = parseDate(dataElements[1])
        val valueDate = parseDate(dataElements[2])
        val amount = parseCreditCardAmount(dataElements.subList(4, 7))
        val otherPartyName = parseString(dataElements[11])

        return CreditCardTransaction(amount, otherPartyName, bookingDate, valueDate)
    }

    private fun parseCreditCardAmount(amountDataElements: List<String>): Money {
        val currency = parseString(amountDataElements[1])
        val isCredit = parseIsCredit(amountDataElements[2])

        var amountString = parseString(amountDataElements[0])

        if (isCredit == false) {
            amountString = "-" + amountString
        }

        return Money(Amount(amountString), currency)
    }


    protected open fun parseBankDetails(dataElementsGroup: String): Kreditinstitutskennung {
        val detailsStrings = getDataElements(dataElementsGroup)

        return parseBankDetails(detailsStrings[0], detailsStrings[1])
    }

    protected open fun parseBankDetails(countryCodeDE: String, bankCodeDE: String): Kreditinstitutskennung {
        return Kreditinstitutskennung(parseInt(countryCodeDE), parseString(bankCodeDE))
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


    protected open fun getDataElementsThatContainsNoMaskSeparators(dataElementGroup: String): List<String> {
        return dataElementGroup.split(Separators.DataElementsSeparator)
    }

    protected open fun getDataElements(dataElementGroup: String): List<String> {
        return splitIntoPartsAndUnmask(dataElementGroup, Separators.DataElementsSeparatorChar)
    }

    /**
     * If separator symbols are used in data values, they are masked with '?'.
     * (e. g. 'https?://' should not be split into two data elements.)
     *
     * Don't split string at masked separators.
     *
     * After string is split, unmask separator
     *
     * Also binary data shouldn't be taken into account.
     */
    protected open fun splitIntoPartsAndUnmask(dataString: String, separator: Char): List<String> {
        val binaryRanges = messageUtils.findBinaryDataRanges(dataString)
        val containsLargeBinaryRanges = binaryRanges.firstOrNull { it.last - it.first > 100 } != null

        val parts = mutableListOf<String>()
        var part = StringBuilder()
        var index = 0

        while (index < dataString.length) {
            val char = dataString[index]

            if (containsLargeBinaryRanges && messageUtils.isInRange(index, binaryRanges)) {
                binaryRanges.forEach { range ->
                    if (range.contains(index)) {
                        part.append(dataString.substring(range))
                        index = range.last
                    }
                }
            }
            else if (isSeparator(char, dataString, separator, index, binaryRanges)) {
                parts.add(part.toString())

                part = StringBuilder()
            }
            else if (isNotMaskingCharacter(char, dataString, separator, index, binaryRanges)) {
                part.append(char)
            }

            index++
        }

        if (part.isNotEmpty()) {
            parts.add(part.toString())
        }

        return parts
    }

    protected open fun isSeparator(char: Char, dataString: String, separator: Char, index: Int, binaryRanges: List<IntRange>): Boolean {
        return char == separator
                && (index == 0 || dataString[index - 1] != Separators.MaskingCharacterChar)
                && messageUtils.isInRange(index, binaryRanges) == false
    }

    protected open fun isNotMaskingCharacter(char: Char, dataString: String, separator: Char, index: Int, binaryRanges: List<IntRange>): Boolean {
        return char != Separators.MaskingCharacterChar
                || index + 1 >= dataString.length
                || dataString[index + 1] != separator
                || messageUtils.isInRange(index, binaryRanges)
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

    protected open fun parseIntToNullIfEmpty(string: String): Int? {
        if (string.isNotEmpty()) {
            return parseNullableInt(string)
        }

        return null
    }

    protected open fun parseNullableInt(mayInt: String): Int? {
        try {
            return parseInt(mayInt)
        } catch (ignored: Exception) { }

        return null
    }

    protected open fun parseAmount(amountString: String, isPositive: Boolean = true): Amount {
        var adjustedAmountString = amountString // Hbci amount format uses comma instead dot as decimal separator

        if (isPositive == false) {
            adjustedAmountString = "-" + adjustedAmountString
        }

        return Amount(adjustedAmountString)
    }

    protected open fun parseNullableDateTime(dataElementGroup: String): Date? {
        val dataElements = getDataElements(dataElementGroup)

        if (dataElements.size >= 2) {
            parseNullableDate(dataElements[0])?.let { date ->
                parseNullableTime(dataElements[1])?.let { time ->
                    return Date(date.millisSinceEpoch + time.millisSinceEpoch) // TODO: is this correct?
                }
            }
        }

        return null
    }

    protected open fun parseDate(dateString: String): Date {
        return Datum.parse(dateString)
    }

    protected open fun parseNullableDate(dateString: String): Date? {
        try {
            return parseDate(dateString)
        } catch (ignored: Exception) { }

        return null
    }

    protected open fun parseTime(timeString: String): Date {
        return Uhrzeit.parse(timeString)
    }

    protected open fun parseNullableTime(timeString: String): Date? {
        try {
            return parseTime(timeString)
        } catch (ignored: Exception) { }

        return null
    }

    protected open fun extractBinaryData(binaryData: String): String {
        if (binaryData.startsWith(Separators.BinaryDataSeparatorChar)) {
            val headerEndIndex = binaryData.indexOf(Separators.BinaryDataSeparatorChar, 2)

            if (headerEndIndex > -1) {
                return binaryData.substring(headerEndIndex + 1)
            }
        }

        return binaryData
    }

}
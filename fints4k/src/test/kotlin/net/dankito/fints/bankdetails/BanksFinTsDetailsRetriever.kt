package net.dankito.fints.bankdetails

import net.dankito.fints.FinTsClient
import net.dankito.fints.banks.InMemoryBankFinder
import net.dankito.fints.callback.NoOpFinTsClientCallback
import net.dankito.fints.messages.MessageBuilder
import net.dankito.fints.messages.MessageBuilderResult
import net.dankito.fints.messages.Separators
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.tan.AuftraggeberkontoErforderlich
import net.dankito.fints.messages.datenelemente.implementierte.tan.BezeichnungDesTanMediumsErforderlich
import net.dankito.fints.messages.datenelemente.implementierte.tan.SmsAbbuchungskontoErforderlich
import net.dankito.fints.model.*
import net.dankito.fints.model.mapper.BankDataMapper
import net.dankito.fints.response.Response
import net.dankito.fints.response.segments.SepaAccountInfoParameters
import net.dankito.fints.response.segments.TanInfo
import net.dankito.fints.response.segments.TanProcedureParameters
import net.dankito.fints.util.Java8Base64Service
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.junit.Ignore
import org.junit.Test
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*


@Ignore // not a real test, run manually to retrieve FinTS information from all banks
class BanksFinTsDetailsRetriever {

    companion object {
        private val OutputFolderDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")

        private val log = LoggerFactory.getLogger(BanksFinTsDetailsRetriever::class.java)
    }


    private val bankFinder = InMemoryBankFinder()

    private val bankDataMapper = BankDataMapper()

    private val product: ProductData = ProductData("15E53C26816138699C7B6A3E8", "1.0.0") // TODO: get version dynamically

    private val messageBuilder = MessageBuilder()

    private val finTsClient = object : FinTsClient(NoOpFinTsClientCallback(), Java8Base64Service()) {

        fun getAndHandleResponseForMessagePublic(message: MessageBuilderResult, dialogContext: DialogContext): Response {
            return getAndHandleResponseForMessage(message, dialogContext)
        }

        fun updateBankDataPublic(bank: BankData, response: Response) {
            super.updateBankData(bank, response)
        }

        fun mapToTanProcedureTypePublic(parameters: TanProcedureParameters): TanProcedureType? {
            return super.mapToTanProcedureType(parameters)
        }
    }


    private val requestNotSuccessful = mutableListOf<BankInfo>()

    private val tanProcedureParameter = mutableMapOf<String, MutableSet<TanProcedureParameters>>()
    private val tanProcedureTypes = mutableMapOf<TanProcedureType?, MutableSet<TanProcedureParameters>>()

    private val tanProcedureParameterTechnicalIdentification = mutableSetOf<String>()
    private val tanProcedureParameterVersionZkaTanProcedure = mutableSetOf<String?>()

    private val requiresSmsAbbuchungskonto = mutableListOf<BankInfo>()
    private val requiresAuftraggeberkonto = mutableListOf<BankInfo>()
    private val requiresChallengeClass = mutableListOf<BankInfo>()
    private val signatureStructured = mutableListOf<BankInfo>()
    private val requiresNameOfTanMedia = mutableListOf<BankInfo>()
    private val requiresHhdUcResponse = mutableListOf<BankInfo>()

    private val doesNotSupportHKTAN6 = mutableListOf<BankInfo>()
    private val doesNotSupportHKSAL5or7 = mutableListOf<BankInfo>()
    private val doesNotSupportHKKAZ5to7 = mutableListOf<BankInfo>()
    private val doesNotSupportHKCCS1 = mutableListOf<BankInfo>()

    private val supportsHhd13 = mutableListOf<BankInfo>()
    private val supportsHhd14 = mutableListOf<BankInfo>()

    private val doesNotSupportPain_001_001_or_003_03 = mutableListOf<BankInfo>()


    @Test
    fun retrieveAllBanksFinTsDetails() {

        val allBanks = bankFinder.getBankList()
        val banksSupportingFinTs = allBanks.filter { it.supportsFinTs3_0 }

        val outputFolder = File("bankData", OutputFolderDateFormat.format(Date()))
        val responsesFolder = File(outputFolder, "responses")
        responsesFolder.mkdirs()

        val csvFile = FileWriter(File(outputFolder, "bank_details.csv"))
        val csvPrinter = CSVPrinter(csvFile, CSVFormat.DEFAULT.withHeader(
            "BLZ", "Name", "Ort", "BPD", "Tanverfahren", "Technische Tanverfahrennamen", "HHD 1.3?", "HHD 1.4?",
            "HKTAN 6?", "HKTAN", "HKSAL 5?", "HKSAL", "HKKAZ 5-7?", "HKKAZ", "HKCAZ", "HKCCS 1?", "HKCCS",
            "pain.001.001.03?", "SEPA Formate", "Sprachen", "Untersstützte Geschäftsvorfälle"
        ))


        val uniqueBanks = banksSupportingFinTs.associateBy { "${it.bankCode}_${it.name}" }
        var bankIndex = 0

        uniqueBanks.forEach { bankName, bankInfo ->
            log.info("[${++bankIndex}] Getting details for $bankName ...")

            getAndSaveBankDetails(bankInfo, responsesFolder, csvPrinter)
        }

        printStatistics()

        csvPrinter.close()
        csvFile.close()
    }


    private fun getAnonymousBankInfo(bank: BankData): Response {
        val dialogContext = DialogContext(bank, CustomerData.Anonymous, product)
        val requestBody = messageBuilder.createAnonymousDialogInitMessage(dialogContext)

        val anonymousBankInfoResponse =
            finTsClient.getAndHandleResponseForMessagePublic(requestBody, dialogContext)

        finTsClient.updateBankDataPublic(bank, anonymousBankInfoResponse)
        return anonymousBankInfoResponse
    }

    private fun getAndSaveBankDetails(bankInfo: BankInfo, responsesFolder: File, csvPrinter: CSVPrinter) {
        val bank = bankDataMapper.mapFromBankInfo(bankInfo)

        val anonymousBankInfoResponse = getAnonymousBankInfo(bank)

        File(responsesFolder, "${bankInfo.bankCode}_${bankInfo.name.replace('/', '-')}").writeText(
            anonymousBankInfoResponse.receivedSegments.joinToString(System.lineSeparator()) { it.segmentString + Separators.SegmentSeparator })

        if (anonymousBankInfoResponse.successful == false) {
            requestNotSuccessful.add(bankInfo)
            log.warn("Did not receive response from bank $bankInfo: ${anonymousBankInfoResponse.receivedSegments.joinToString(System.lineSeparator()) { it.segmentString + Separators.SegmentSeparator }}")

            return
        }


        val supportsHKTAN6 = supportsJobInVersion(bank, "HKTAN", 6)
        val supportsHKSAL5or7 = supportsJobInVersion(bank, "HKSAL", listOf(5, 7))
        val supportsHKKAZ5to7 = supportsJobInVersion(bank, "HKKAZ", listOf(5, 6, 7))
        val supportsHKCCS1 = supportsJobInVersion(bank, "HKCCS", 1)

        val tanInfo = anonymousBankInfoResponse.receivedSegments.filterIsInstance(TanInfo::class.java)
        val tanProcedureParameters = tanInfo.flatMap { it.tanProcedureParameters.procedureParameters }
        val supportedTanProcedures = tanProcedureParameters.map { it.technicalTanProcedureIdentification }
        val hhd13Supported = supportedTanProcedures.firstOrNull { it.startsWith("hhd1.3", true) } != null
        val hhd14Supported = supportedTanProcedures.firstOrNull { it.startsWith("hhd1.4", true) } != null

        val supportedHKTANVersions = tanInfo.map { it.segmentVersion }
        val supportedHKSALVersions = getSupportedVersions(bank, "HKSAL")
        val supportedHKKAZVersions = getSupportedVersions(bank, "HKKAZ")
        val supportedHKCAZVersions = getSupportedVersions(bank, "HKCAZ")
        val supportedHKCCSVersions = getSupportedVersions(bank, "HKCCS")

        val sepaAccountInfoParameters = anonymousBankInfoResponse.receivedSegments.filterIsInstance<SepaAccountInfoParameters>()
        val supportedSepaFormats = sepaAccountInfoParameters.flatMap { it.supportedSepaFormats }.map { it.substring(it.indexOf(":xsd:") + ":xsd:".length) }
        val supportsPain_001_001_or_003_03 = supportedSepaFormats.firstOrNull { it.contains("pain.001.001.03") or it.contains("pain.001.003.03") } != null

        csvPrinter.printRecord(bankInfo.bankCode, bankInfo.name, bankInfo.city,
            bank.bpdVersion,
            bank.supportedTanProcedures.joinToString(", ") { it.securityFunction.code + ": " + it.displayName + " (" + it.type + ")" },
            supportedTanProcedures.joinToString(", "),
            hhd13Supported,
            hhd14Supported,
            supportsHKTAN6,
            supportedHKTANVersions.joinToString(", "),
            supportsHKSAL5or7,
            supportedHKSALVersions.joinToString(", "),
            supportsHKKAZ5to7,
            supportedHKKAZVersions.joinToString(", "),
            supportedHKCAZVersions.joinToString(", "),
            supportsHKCCS1,
            supportedHKCCSVersions.joinToString(", "),
            supportsPain_001_001_or_003_03,
            supportedSepaFormats.joinToString(", "),
            bank.supportedLanguages.filter { it != Dialogsprache.German }.joinToString(", ") { it.name },
            bank.supportedJobs.joinToString(", ") { it.jobName + " " + it.segmentVersion }
        )

        tanProcedureParameters.forEach { procedureParameter ->
            if (tanProcedureParameter.containsKey(procedureParameter.procedureName) == false) {
                tanProcedureParameter.put(procedureParameter.procedureName, mutableSetOf(procedureParameter))
            }
            else {
                tanProcedureParameter[procedureParameter.procedureName]?.add(procedureParameter)
            }

            val tanProcedureType = finTsClient.mapToTanProcedureTypePublic(procedureParameter)
            if (tanProcedureTypes.containsKey(tanProcedureType) == false) {
                tanProcedureTypes.put(tanProcedureType, mutableSetOf(procedureParameter))
            }
            else {
                tanProcedureTypes[tanProcedureType]?.add(procedureParameter)
            }

            tanProcedureParameterTechnicalIdentification.add(procedureParameter.technicalTanProcedureIdentification)
            tanProcedureParameterVersionZkaTanProcedure.add(procedureParameter.versionZkaTanProcedure)

            if (procedureParameter.smsDebitAccountRequired == SmsAbbuchungskontoErforderlich.SmsAbbuchungskontoMussAngegebenWerden) {
                requiresSmsAbbuchungskonto.add(bankInfo)
            }
            if (procedureParameter.initiatorAccountRequired == AuftraggeberkontoErforderlich.AuftraggeberkontoMussAngegebenWerdenWennImGeschaeftsvorfallEnthalten) {
                requiresAuftraggeberkonto.add(bankInfo)
            }
            if (procedureParameter.challengeClassRequired) {
                requiresChallengeClass.add(bankInfo)
            }
            if (procedureParameter.signatureStructured) {
                signatureStructured.add(bankInfo)
            }
            if (procedureParameter.nameOfTanMediaRequired == BezeichnungDesTanMediumsErforderlich.BezeichnungDesTanMediumsMussAngegebenWerden) {
                requiresNameOfTanMedia.add(bankInfo)
            }
            if (procedureParameter.hhdUcResponseRequired) {
                requiresHhdUcResponse.add(bankInfo)
            }
        }

        if (supportsHKTAN6 == false) {
            doesNotSupportHKTAN6.add(bankInfo)
        }
        if (supportsHKSAL5or7 == false) {
            doesNotSupportHKSAL5or7.add(bankInfo)
        }
        if (supportsHKKAZ5to7 == false) {
            doesNotSupportHKKAZ5to7.add(bankInfo)
        }
        if (supportsHKCCS1 == false) {
            doesNotSupportHKCCS1.add(bankInfo)
        }

        if (hhd13Supported) {
            supportsHhd13.add(bankInfo)
        }
        if (hhd14Supported) {
            supportsHhd14.add(bankInfo)
        }

        if (supportsPain_001_001_or_003_03 == false) {
            doesNotSupportPain_001_001_or_003_03.add(bankInfo)
        }
    }

    private fun getSupportedVersions(bank: BankData, jobName: String): List<Int> {
        return bank.supportedJobs.filter { it.jobName == jobName }.map { it.segmentVersion }
    }

    private fun supportsJobInVersion(bank: BankData, jobName: String, version: Int): Boolean {
        return supportsJobInVersion(bank, jobName, listOf(version))
    }

    private fun supportsJobInVersion(bank: BankData, jobName: String, versions: List<Int>): Boolean {
        return bank.supportedJobs.firstOrNull { it.jobName == jobName && versions.contains(it.segmentVersion) } != null
    }


    private fun printStatistics() {
        log.info("Did not receive response from Banks ${printBanks(requestNotSuccessful)}")

        log.info("Mapped tanProcedureTypes: ${tanProcedureTypes.map { System.lineSeparator() + it.key + ": " + it.value.map { it.procedureName + " " + it.zkaTanProcedure + " " + it.technicalTanProcedureIdentification + " (" + it.descriptionToShowToUser + ")" }.toSet().joinToString(", ") }}\n\n")
        log.info("TanProcedureParameters:${tanProcedureParameter.map { System.lineSeparator() + it.key + ": " + it.value.map { it.securityFunction.code + " " + it.zkaTanProcedure + " " + it.technicalTanProcedureIdentification + " (" + it.descriptionToShowToUser + ")" }.toSet().joinToString(", ") } }\n\n")

        log.info("TanProcedureParameters TechnicalIdentification:${tanProcedureParameterTechnicalIdentification.joinToString(", ") } \n\n")
        log.info("TanProcedureParameters VersionZkaTanProcedure:${tanProcedureParameterVersionZkaTanProcedure.joinToString(", ") } \n\n")

        log.info("Requires SmsAbbuchungskonto ${printBanks(requiresSmsAbbuchungskonto)}") // no (only 2)
        log.info("Requires Auftraggeberkonto ${printBanks(requiresAuftraggeberkonto)}") // yes, a lot of (12631)
        log.info("Requires ChallengeClass ${printBanks(requiresChallengeClass)}") // no
        log.info("Has structured signature ${printBanks(signatureStructured)}") // yes, a lot of (12651)
        log.info("Requires NameOfTanMedia ${printBanks(requiresNameOfTanMedia)}") // yes, a lot of (912)
        log.info("Requires HhdUcResponse ${printBanks(requiresHhdUcResponse)}") // no (only 2)

        log.info("Banks supporting HHD 1.3 (${supportsHhd13.size}):${printBanks(supportsHhd13)}")
        log.info("Banks supporting HHD 1.4 (${supportsHhd14.size}):${printBanks(supportsHhd14)}")

        log.info("Banks not supporting HKTAN 6 ${printBanks(doesNotSupportHKTAN6)}")
        log.info("Banks not supporting HKSAL 5 or 7 ${printBanks(doesNotSupportHKSAL5or7)}")
        log.info("Banks not supporting HKKAZ 5-7 ${printBanks(doesNotSupportHKKAZ5to7)}")
        log.info("Banks not supporting HKCCS 1 ${printBanks(doesNotSupportHKCCS1)}")

        log.info("Banks not supporting pain.001.001.03 or pain.001.003.03 ${printBanks(doesNotSupportPain_001_001_or_003_03)}")
    }

    private fun printBanks(banks: List<BankInfo>): String {
        return "(${banks.size}):${ banks.joinToString { System.lineSeparator() + it } }\n\n\n"
    }

}
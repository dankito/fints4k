package net.dankito.banking.banklistcreator.parser

import net.dankito.banking.bankfinder.DetailedBankInfo
import net.dankito.banking.banklistcreator.parser.model.BankCodeListEntry
import net.dankito.banking.banklistcreator.parser.model.ServerAddressesListEntry
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage
import org.slf4j.LoggerFactory
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter
import org.xlsx4j.sml.Cell
import org.xlsx4j.sml.Row
import org.xlsx4j.sml.SheetData
import java.io.File


/**
 * Parses the list of German banks from Deutsche Kreditwirtschaft you can retrieve by registering here:
 * https://www.hbci-zka.de/register/hersteller.htm
 */
open class DeutscheKreditwirtschaftBankListParser {

    companion object {
        private val log = LoggerFactory.getLogger(DeutscheKreditwirtschaftBankListParser::class.java)
    }


    open fun parse(bankListFile: File): List<DetailedBankInfo> {
        val xlsxPkg = SpreadsheetMLPackage.load(bankListFile)

        val workbookPart = xlsxPkg.getWorkbookPart()
        val sheets = workbookPart.contents.sheets.sheet
        val formatter = DataFormatter()

        var serverAddressesList = listOf<ServerAddressesListEntry>()
        var bankCodesList = listOf<BankCodeListEntry>()

        for (index in 0 until sheets.size) {
            log.info("\nParsing sheet ${sheets[index].name}:\n")
            val sheet = workbookPart.getWorksheet(index)
            val workSheetData = sheet.contents.sheetData

            if (isListWithFinTsServerAddresses(workSheetData, formatter)) {
                serverAddressesList = parseListWithFinTsServerAddresses(workSheetData, formatter)
            }
            else if (isBankCodeList(workSheetData, formatter)) {
                bankCodesList = parseBankCodeList(workSheetData, formatter)
            }
        }

        return mapBankCodeAndServerAddressesList(bankCodesList, serverAddressesList)
    }

    protected open fun mapBankCodeAndServerAddressesList(banks: List<BankCodeListEntry>,
                                                  serverAddresses: List<ServerAddressesListEntry>): List<DetailedBankInfo> {

        val serverAddressesByBankCode = mutableMapOf<String, MutableList<ServerAddressesListEntry>>()
        serverAddresses.forEach { serverAddress ->
            if (serverAddressesByBankCode.containsKey(serverAddress.bankCode) == false) {
                serverAddressesByBankCode.put(serverAddress.bankCode, mutableListOf(serverAddress))
            }
            else {
                serverAddressesByBankCode[serverAddress.bankCode]!!.add(serverAddress)
            }
        }

        return banks.map { mapToBankInfo(it, serverAddressesByBankCode as Map<String, List<ServerAddressesListEntry>>) }
    }

    protected open fun mapToBankInfo(bank: BankCodeListEntry,
                              serverAddressesByBankCode: Map<String, List<ServerAddressesListEntry>>): DetailedBankInfo {

        val serverAddress = findServerAddress(bank, serverAddressesByBankCode)

        return DetailedBankInfo(
            bank.bankName,
            bank.bankCode,
            bank.bic,
            bank.postalCode,
            bank.city,
            serverAddress?.pinTanAddress,
            serverAddress?.pinTanVersion,
            bank.checksumMethod,
            bank.oldBankCode
        )
    }

    protected open fun findServerAddress(bankCode: BankCodeListEntry,
                                  serverAddressesByBankCode: Map<String, List<ServerAddressesListEntry>>
    ): ServerAddressesListEntry? {

        serverAddressesByBankCode[bankCode.bankCode]?.let { serverAddresses ->
            serverAddresses.firstOrNull { it.city == bankCode.city }?.let {
                return it
            }

            return serverAddresses[0]
        }

        return null
    }


    protected open fun isListWithFinTsServerAddresses(workSheetData: SheetData, formatter: DataFormatter): Boolean {
        return hasHeaders(workSheetData, formatter, listOf("BLZ", "PIN/TAN-Zugang URL"))
    }

    protected open fun parseListWithFinTsServerAddresses(workSheetData: SheetData, formatter: DataFormatter):
            List<ServerAddressesListEntry> {

        val entries = mutableListOf<ServerAddressesListEntry>()

        val headerRow = workSheetData.row[0]
        val headerNames = headerRow.c.map { getCellText(it, formatter) }

        val bankNameColumnIndex = headerNames.indexOf("Institut")
        val bankCodeColumnIndex = headerNames.indexOf("BLZ")
        val bicColumnIndex = headerNames.indexOf("BIC")
        val cityColumnIndex = headerNames.indexOf("Ort")
        val pinTanAddressColumnIndex = headerNames.indexOf("PIN/TAN-Zugang URL")
        val pinTanVersionColumnIndex = headerNames.indexOf("Version")

        for (row in workSheetData.row.subList(1, workSheetData.row.size)) {
            parseToServerAddressesListEntry(row, formatter, bankNameColumnIndex, bankCodeColumnIndex, bicColumnIndex,
                cityColumnIndex, pinTanAddressColumnIndex, pinTanVersionColumnIndex)?.let { entry ->
                entries.add(entry)
            }
        }

        return entries
    }

    protected open fun parseToServerAddressesListEntry(row: Row, formatter: DataFormatter, bankNameColumnIndex: Int,
                                                bankCodeColumnIndex: Int, bicColumnIndex: Int, cityColumnIndex: Int,
                                                pinTanAddressColumnIndex: Int, pinTanVersionColumnIndex: Int):
            ServerAddressesListEntry? {

        try {
            val bankCode = getCellText(row, bankCodeColumnIndex, formatter)

            if (bankCode.isNotEmpty()) { // filter out empty rows

                return ServerAddressesListEntry(
                    getCellText(row, bankNameColumnIndex, formatter),
                    bankCode,
                    getCellText(row, bicColumnIndex, formatter),
                    getCellText(row, cityColumnIndex, formatter),
                    getCellText(row, pinTanAddressColumnIndex, formatter),
                    getCellText(row, pinTanVersionColumnIndex, formatter)
                )
            }
        } catch (e: Exception) {
            log.error("Could not parse row ${getRowAsString(row, formatter)} to BankCodeListEntry", e)
        }

        return null
    }


    protected open fun isBankCodeList(workSheetData: SheetData, formatter: DataFormatter): Boolean {
        return hasHeaders(workSheetData, formatter, listOf("Bankleitzahl", "Merkmal"))
    }

    protected open fun parseBankCodeList(workSheetData: SheetData, formatter: DataFormatter): List<BankCodeListEntry> {
        val entries = mutableListOf<BankCodeListEntry>()

        val headerRow = workSheetData.row[0]
        val headerNames = headerRow.c.map { getCellText(it, formatter) }

        val bankNameColumnIndex = headerNames.indexOf("Bezeichnung")
        val bankCodeColumnIndex = headerNames.indexOf("Bankleitzahl")
        val bicColumnIndex = headerNames.indexOf("BIC")
        val postalCodeColumnIndex = headerNames.indexOf("PLZ")
        val cityColumnIndex = headerNames.indexOf("Ort")
        val checksumMethodColumnIndex = headerNames.indexOf("Prüfziffer-berechnungs-methode")
        val bankCodeDeletedColumnIndex = headerNames.indexOf("Bankleitzahl-löschung")
        val newBankCodeColumnIndex = headerNames.indexOf("Nachfolge-Bankleitzahl")

        var lastParsedEntry: BankCodeListEntry? = null

        for (row in workSheetData.row.subList(1, workSheetData.row.size)) {
            parseToBankCodeListEntry(row, formatter, bankNameColumnIndex, bankCodeColumnIndex, bicColumnIndex,
                postalCodeColumnIndex, cityColumnIndex, checksumMethodColumnIndex, bankCodeDeletedColumnIndex,
                newBankCodeColumnIndex)?.let { entry ->
                // if the following banks have the same BIC, the BIC is only given for the first bank -> get BIC from previous bank
                if (entry.bic.isEmpty() &&
                        (entry.bankCode == lastParsedEntry?.bankCode || entry.bankCode == lastParsedEntry?.oldBankCode)) {
                    entry.bic = lastParsedEntry?.bic!!
                }

                entries.add(entry)

                lastParsedEntry = entry
            }
        }

        updateDeletedBanks(entries)

        return entries
    }

    protected open fun parseToBankCodeListEntry(row: Row, formatter: DataFormatter, bankNameColumnIndex: Int,
                                         bankCodeColumnIndex: Int, bicColumnIndex: Int, postalCodeColumnIndex: Int,
                                         cityColumnIndex: Int, checksumMethodColumnIndex: Int,
                                         bankCodeDeletedColumnIndex: Int, newBankCodeColumnIndex: Int): BankCodeListEntry? {

        try {
            val bankCode = getCellText(row, bankCodeColumnIndex, formatter)

            if (bankCode.isNotEmpty()) { // filter out empty rows
                var newBankCode: String? = null
                val isBankCodeDeleted = getCellText(row, bankCodeDeletedColumnIndex, formatter) == "1"
                val newBankCodeCellText = getCellText(row, newBankCodeColumnIndex, formatter)
                if (isBankCodeDeleted && newBankCodeCellText.isNotEmpty() && newBankCodeCellText != "00000000") {
                    newBankCode = newBankCodeCellText
                }

                return BankCodeListEntry(
                    getCellText(row, bankNameColumnIndex, formatter),
                    newBankCode ?: bankCode,
                    getCellText(row, bicColumnIndex, formatter),
                    getCellText(row, postalCodeColumnIndex, formatter),
                    getCellText(row, cityColumnIndex, formatter),
                    getCellText(row, checksumMethodColumnIndex, formatter),
                    if (newBankCode != null) bankCode else newBankCode
                )
            }
        } catch (e: Exception) {
            log.error("Could not parse row ${getRowAsString(row, formatter)} to BankCodeListEntry", e)
        }

        return null
    }

    /**
     * Deleted banks may not have a BIC. This method fixes this
     */
    protected open fun updateDeletedBanks(banks: MutableList<BankCodeListEntry>) {
        val banksByCode = banks.associateBy { it.bankCode }

        val deletedBanks = banks.filter { it.isBankCodeDeleted }

        for (deletedBank in deletedBanks) {
            banksByCode[deletedBank.bankCode]?.let { newBank ->
                deletedBank.bic = newBank.bic
            }
        }
    }


    protected open fun hasHeaders(workSheetData: SheetData, formatter: DataFormatter, headerNames: List<String>): Boolean {
        if (workSheetData.row.isNotEmpty()) {
            val headerRow = workSheetData.row[0]

            val rowHeaderNames = headerRow.c.map { getCellText(it, formatter) }

            return rowHeaderNames.containsAll(headerNames)
        }

        return false
    }

    protected open fun getCellText(row: Row, columnIndex: Int, formatter: DataFormatter): String {
        return getCellText(row.c[columnIndex], formatter)
    }

    protected open fun getCellText(cell: Cell, formatter: DataFormatter): String {
        if (cell.f != null) { // cell with formular
            return cell.v
        }
        return formatter.formatCellValue(cell)
    }

    protected open fun getRowAsString(row: Row, formatter: DataFormatter): String {
        return row.c.joinToString("\t|", "|\t", "\t|") { getCellText(it, formatter) }
    }

}
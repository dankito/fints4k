package net.dankito.banking.util

import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import org.kapott.hbci.GV_Result.GVRKUms
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.text.SimpleDateFormat


open class AccountTransactionMapper {

    companion object {
        protected val DateStartString = "DATUM "
        protected val DateEndString = " UHR"

        protected val DateTimeFormat = SimpleDateFormat("dd.MM.yyyy,HH.mm")

        protected val DateFormat = SimpleDateFormat("dd.MM.yyyy,")

        private val log = LoggerFactory.getLogger(AccountTransactionMapper::class.java)
    }


    open fun mapAccountTransactions(bankAccount: BankAccount, result: GVRKUms): List<AccountTransaction> {
        val entries = ArrayList<AccountTransaction>()

        result.flatData.forEach { transaction ->
            entries.add(mapAccountingEntry(bankAccount, transaction))
        }

        log.debug("Retrieved ${result.flatData.size} accounting entries")

        return entries.sortedByDescending { it.bookingDate }
    }

    protected open fun mapAccountingEntry(bankAccount: BankAccount, transaction: GVRKUms.UmsLine): AccountTransaction {

        val result = AccountTransaction(BigDecimal.valueOf(transaction.value.longValue).divide(BigDecimal.valueOf(100)), transaction.bdate, transaction.usage.joinToString(""),
            if (transaction.other.name2.isNullOrBlank() == false) transaction.other.name + " " + transaction.other.name2 else transaction.other.name,
            if (transaction.other.bic != null) transaction.other.bic else transaction.other.blz,
            if (transaction.other.iban != null) transaction.other.iban else transaction.other.number,
            transaction.text, BigDecimal.valueOf(transaction.saldo.value.longValue), transaction.value.curr, bankAccount)

//        mapUsage(transaction, result)

        return result
    }

    /**
     * From https://sites.google.com/a/crem-solutions.de/doku/version-2012-neu/buchhaltung/03-zahlungsverkehr/05-e-banking/technische-beschreibung-der-mt940-sta-datei:
     *
     * Weitere 4 Verwendungszwecke können zu den Feldschlüsseln 60 bis 63 eingestellt werden.
     * Jeder Bezeichner [z.B. EREF+] muss am Anfang eines Subfeldes [z. B. ?21] stehen.
     * Bei Längenüberschreitung wird im nachfolgenden Subfeld ohne Wiederholung des Bezeichners fortgesetzt. Bei Wechsel des Bezeichners ist ein neues Subfeld zu beginnen.
     * Belegung in der nachfolgenden Reihenfolge, wenn vorhanden:
     * EREF+[ Ende-zu-Ende Referenz ] (DD-AT10; CT-AT41 - Angabe verpflichtend; NOTPROVIDED wird nicht eingestellt.)
     * KREF+[Kundenreferenz]
     * MREF+[Mandatsreferenz] (DD-AT01 - Angabe verpflichtend)
     * CRED+[Creditor Identifier] (DD-AT02 - Angabe verpflichtend bei SEPA-Lastschriften, nicht jedoch bei SEPA-Rücklastschriften)
     * DEBT+[Originators Identification Code](CT-AT10- Angabe verpflichtend,)
     * Entweder CRED oder DEBT
     *
     * optional zusätzlich zur Einstellung in Feld 61, Subfeld 9:
     *
     * COAM+ [Compensation Amount / Summe aus Auslagenersatz und Bearbeitungsprovision bei einer nationalen Rücklastschrift sowie optionalem Zinsausgleich.]
     * OAMT+[Original Amount] Betrag der ursprünglichen Lastschrift
     *
     * SVWZ+[SEPA-Verwendungszweck] (DD-AT22; CT-AT05 -Angabe verpflichtend, nicht jedoch bei R-Transaktionen)
     * ABWA+[Abweichender Überweisender] (CT-AT08) / Abweichender Zahlungsempfänger (DD-AT38) ] (optional)
     * ABWE+[Abweichender Zahlungsemp-fänger (CT-AT28) / Abweichender Zahlungspflichtiger ((DD-AT15)] (optional)
     */
//    protected open fun mapUsage(buchung: GVRKUms.UmsLine, entry: AccountingEntry) {
//        var lastUsageLineType = UsageLineType.ContinuationFromLastLine
//        var typeValue = ""
//
//        buchung.usage.forEach { line ->
//            val (type, adjustedString) = getUsageLineType(line, entry)
//
//            if (type == UsageLineType.ContinuationFromLastLine) {
//                typeValue += (if(adjustedString[0].isUpperCase()) " " else "") + adjustedString
//            }
//            else if (lastUsageLineType != type) {
//                if (lastUsageLineType != UsageLineType.ContinuationFromLastLine) {
//                    setUsageLineValue(entry, lastUsageLineType, typeValue)
//                }
//
//                typeValue = adjustedString
//                lastUsageLineType = type
//            }
//
//            tryToParseBookingDateFromUsageLine(entry, adjustedString, typeValue)
//        }
//
//        if(lastUsageLineType != UsageLineType.ContinuationFromLastLine) {
//            setUsageLineValue(entry, lastUsageLineType, typeValue)
//        }
//    }
//
//    protected open fun setUsageLineValue(entry: AccountingEntry, lastUsageLineType: UsageLineType, typeValue: String) {
//        entry.parsedUsages.add(typeValue)
//
//        when (lastUsageLineType) {
//            UsageLineType.EREF -> entry.endToEndReference = typeValue
//            UsageLineType.KREF -> entry.kundenreferenz = typeValue
//            UsageLineType.MREF -> entry.mandatsreferenz = typeValue
//            UsageLineType.CRED -> entry.creditorIdentifier = typeValue
//            UsageLineType.DEBT -> entry.originatorsIdentificationCode = typeValue
//            UsageLineType.COAM -> entry.compensationAmount = typeValue
//            UsageLineType.OAMT -> entry.originalAmount = typeValue
//            UsageLineType.SVWZ -> entry.sepaVerwendungszweck = typeValue
//            UsageLineType.ABWA -> entry.abweichenderAuftraggeber = typeValue
//            UsageLineType.ABWE -> entry.abweichenderZahlungsempfaenger = typeValue
//            UsageLineType.NoSpecialType -> entry.usageWithNoSpecialType = typeValue
//        }
//    }
//
//    protected open fun getUsageLineType(line: String, entry: AccountingEntry): Pair<UsageLineType, String> {
//        return when {
//            line.startsWith("EREF+") -> Pair(UsageLineType.EREF, line.substring(5))
//            line.startsWith("KREF+") -> Pair(UsageLineType.KREF, line.substring(5))
//            line.startsWith("MREF+") -> Pair(UsageLineType.MREF, line.substring(5))
//            line.startsWith("CRED+") -> Pair(UsageLineType.CRED, line.substring(5))
//            line.startsWith("DEBT+") -> Pair(UsageLineType.DEBT, line.substring(5))
//            line.startsWith("COAM+") -> Pair(UsageLineType.COAM, line.substring(5))
//            line.startsWith("OAMT+") -> Pair(UsageLineType.OAMT, line.substring(5))
//            line.startsWith("SVWZ+") -> Pair(UsageLineType.SVWZ, line.substring(5))
//            line.startsWith("ABWA+") -> Pair(UsageLineType.ABWA, line.substring(5))
//            line.startsWith("ABWE+") -> Pair(UsageLineType.ABWE, line.substring(5))
//            entry.usage.startsWith(line) -> Pair(UsageLineType.NoSpecialType, line)
//            else -> Pair(UsageLineType.ContinuationFromLastLine, line)
//        }
//    }
//
//    protected open fun tryToParseBookingDateFromUsageLine(entry: AccountingEntry, currentLine: String, typeLine: String) {
//        if (currentLine.startsWith(DateStartString)) {
//            tryToParseBookingDateFromUsageLine(entry, currentLine)
//        }
//        else if (typeLine.startsWith(DateStartString)) {
//            tryToParseBookingDateFromUsageLine(entry, typeLine)
//        }
//    }
//
//    protected open fun tryToParseBookingDateFromUsageLine(entry: AccountingEntry, line: String) {
//        var dateString = line.replace(DateStartString, "")
//        val index = dateString.indexOf(DateEndString)
//        if (index > 0) {
//            dateString = dateString.substring(0, index)
//        }
//
//        try {
//            entry.bookingDate = DateTimeFormat.parse(dateString)
//        } catch (e: Exception) {
//            try {
//                entry.bookingDate = DateFormat.parse(dateString)
//            } catch (secondException: Exception) {
//                log.debug("Could not parse '$dateString' from '$line' to a Date", e)
//            }
//        }
//    }

}
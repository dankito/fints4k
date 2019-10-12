package net.dankito.fints.transactions.mt940

import net.dankito.fints.transactions.mt940.model.*
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


/*
4.1. SWIFT Supported Characters

a until z
A until Z
0 until 9
/ ‐ ? : ( ) . , ' + { }
CR LF Space
Although part of the character set, the curly brackets are permitted as delimiters and cannot be used within the text of
user‐to‐user messages.
Character ”‐” is not permitted as the first character of the line.
None of lines include only Space.
 */
open class Mt940Parser : IMt940Parser {

    companion object {
        val AccountStatementsSeparatorPattern = Pattern.compile("^\\s*-\\s*\$") // a line only with '-' and may other white space characters

        val AccountStatementFieldSeparatorPattern = Pattern.compile(":\\d\\d\\w?:")


        val TransactionReferenceNumberCode = "20"

        val ReferenceReferenceNumberCode = "21"

        val AccountIdentificationCode = "25"

        val StatementNumberCode = "28C"

        val OpeningBalanceCode = "60"

        val TransactionTurnoverCode = "61"

        val TransactionDetailsCode = "86"

        val ClosingBalanceCode = "62"


        val DateFormat = SimpleDateFormat("yyMMdd")

        val CreditDebitCancellationPattern = Pattern.compile("C|D|RC|RD")

        val AmountPattern = Pattern.compile("\\d+,\\d*")

        val UsageTypePattern = Pattern.compile("\\w{4}\\+")


        private val log = LoggerFactory.getLogger(Mt940Parser::class.java)
    }


    override fun parseMt940String(mt940String: String): List<AccountStatement> {
        try {
            val singleAccountStatementsStrings = splitIntoSingleAccountStatements(mt940String)

            return singleAccountStatementsStrings.mapNotNull { parseAccountStatement(it) }
        } catch (e: Exception) {
            log.error("Could not parse account statements from MT940 string:\n$mt940String", e)
        }

        return listOf()
    }


    protected open fun splitIntoSingleAccountStatements(mt940String: String): List<String> {
        val accountStatements = mutableListOf<String>()

        val lines = mt940String.split("\n")
        var lastMatchedLine = 0
        lines.forEachIndexed { index, line ->
            if (line == "-") {
                val test = AccountStatementsSeparatorPattern.matcher(line).matches() // TODO

                accountStatements.add(lines.subList(lastMatchedLine, index).joinToString("\n"))

                lastMatchedLine = index + 1
            }
        }

        return accountStatements
    }


    protected open fun parseAccountStatement(accountStatementString: String): AccountStatement? {
        try {
            val fieldsByCode = splitIntoFields(accountStatementString)

            return parseAccountStatement(fieldsByCode)
        } catch (e: Exception) {
            log.error("Could not parse account statement:\n$accountStatementString", e)
        }

        return null
    }

    protected open fun splitIntoFields(accountStatementString: String): List<Pair<String, String>> {
        val matcher = AccountStatementFieldSeparatorPattern.matcher(accountStatementString)

        val result = mutableListOf<Pair<String, String>>()
        var lastMatchEnd = 0
        var lastMatchedCode = ""

        while (matcher.find()) {
            if (lastMatchEnd > 0) {
                val previousStatement = accountStatementString.substring(lastMatchEnd, matcher.start()).replace("\n", "")
                result.add(Pair(lastMatchedCode, previousStatement))
            }

            lastMatchedCode = matcher.group().replace(":", "")
            lastMatchEnd = matcher.end()
        }

        if (lastMatchEnd > 0) {
            val previousStatement = accountStatementString.substring(lastMatchEnd, accountStatementString.length).replace("\n", "")
            result.add(Pair(lastMatchedCode, previousStatement))
        }

        return result
    }

    protected open fun parseAccountStatement(fieldsByCode: List<Pair<String, String>>): AccountStatement? {
        val statementAndMaySequenceNumber = getFieldValue(fieldsByCode, StatementNumberCode)
        val accountIdentification = getFieldValue(fieldsByCode, AccountIdentificationCode)
        val openingBalancePair = fieldsByCode.first { it.first.startsWith(OpeningBalanceCode) }
        val closingBalancePair = fieldsByCode.first { it.first.startsWith(ClosingBalanceCode) }

        return AccountStatement(
            getFieldValue(fieldsByCode, TransactionReferenceNumberCode),
            getOptionalFieldValue(fieldsByCode, ReferenceReferenceNumberCode),
            parseBankCodeSwiftCodeOrIban(accountIdentification),
            parseAccountNumber(accountIdentification),
            parseStatementNumber(statementAndMaySequenceNumber),
            parseSheetNumber(statementAndMaySequenceNumber),
            parseBalance(openingBalancePair.first, openingBalancePair.second),
            parseAccountStatementTransactions(fieldsByCode),
            parseBalance(closingBalancePair.first, closingBalancePair.second)
        )
    }

    protected open fun getFieldValue(fieldsByCode: List<Pair<String, String>>, code: String): String {
        return fieldsByCode.first { it.first == code }.second
    }

    protected open fun getOptionalFieldValue(fieldsByCode: List<Pair<String, String>>, code: String): String? {
        return fieldsByCode.firstOrNull { it.first == code }?.second
    }

    protected open fun parseBankCodeSwiftCodeOrIban(accountIdentification: String): String {
        val parts = accountIdentification.split('/')

        return parts[0]
    }

    protected open fun parseAccountNumber(accountIdentification: String): String? {
        val parts = accountIdentification.split('/')

        if (parts.size > 0) {
            return parts[1]
        }

        return null
    }

    protected open fun parseStatementNumber(statementAndMaySheetNumber: String): Int {
        val parts = statementAndMaySheetNumber.split('/')

        // val isSupported = statementNumber != "00000"
        return parts[0].toInt()
    }

    protected open fun parseSheetNumber(statementAndMaySheetNumber: String): Int? {
        val parts = statementAndMaySheetNumber.split('/')

        if (parts.size > 0) {
            return parts[1].toInt()
        }

        return null
    }

    protected open fun parseBalance(code: String, fieldValue: String): Balance {
        val isIntermediate = code.endsWith("M")

        val isDebit = fieldValue.startsWith("D")
        val bookingDateString = fieldValue.substring(1, 7)
        val statementDate = parseDate(bookingDateString)
        val currency = fieldValue.substring(7, 10)
        val amountString = fieldValue.substring(10)
        val amount = parseAmount(amountString)

        return Balance(isIntermediate, !!!isDebit, statementDate, currency, amount)
    }

    protected open fun parseAccountStatementTransactions(fieldsByCode: List<Pair<String, String>>): List<Transaction> {
        val transactions = mutableListOf<Transaction>()

        fieldsByCode.forEachIndexed { index, pair ->
            if (pair.first == TransactionTurnoverCode) {
                val turnover = parseTurnover(pair.second)

                val nextPair = if (index < fieldsByCode.size - 1) fieldsByCode.get(index + 1) else null
                val details = if (nextPair?.first == TransactionDetailsCode) parseNullableTransactionDetails(nextPair?.second) else null

                transactions.add(Transaction(turnover, details))
            }
        }

        return transactions
    }

    protected open fun parseTurnover(fieldValue: String): Turnover {
        val valueDateString = fieldValue.substring(0, 6)
        val valueDate = parseDate(valueDateString)

        val creditMarkMatcher = CreditDebitCancellationPattern.matcher(fieldValue)
        creditMarkMatcher.find()
        val isDebit = creditMarkMatcher.group().endsWith('D')
        val isCancellation = creditMarkMatcher.group().startsWith('R')

        val bookingDateString = if (creditMarkMatcher.start() > 6) fieldValue.substring(6, 10) else null
        val bookingDate = bookingDateString?.let { // bookingDateString has format MMdd -> add year from valueDateString
            parseDate(valueDateString.substring(0, 2) + bookingDateString)
        }

        val amountMatcher = AmountPattern.matcher(fieldValue)
        amountMatcher.find()
        val amountString = amountMatcher.group()
        val amount = parseAmount(amountString)

        val amountEndIndex = amountMatcher.end()

        /**
         * S    SWIFT transfer  For entries related to SWIFT transfer instructions and subsequent charge messages.
         *
         * N    Non-SWIFT       For entries related to payment and transfer instructions, including transfer related charges messages, not sent through SWIFT or where an alpha description is preferred.
         *
         * F    First advice    For entries being first advised by the statement (items originated by the account servicing institution).
         */
        val transactionType = fieldValue.substring(amountEndIndex, amountEndIndex + 1) // transaction type is 'N', 'S' or 'F'

        val bookingKeyStart = amountEndIndex + 1
        val bookingKey = fieldValue.substring(bookingKeyStart, bookingKeyStart + 3) // TODO: parse codes, p. 178

        var customerReference = fieldValue.substring(bookingKeyStart + 3)
        var bankReference: String? = null
        if (customerReference.contains("//")) {
            val indexOfDoubleSlash = customerReference.indexOf("//")

            bankReference = customerReference.substring(indexOfDoubleSlash + 2)
            customerReference = customerReference.substring(0, indexOfDoubleSlash)
        }

        return Turnover(!!!isDebit, isCancellation, valueDate, bookingDate, null, amount, bookingKey,
            customerReference, bankReference)
    }

    protected open fun parseNullableTransactionDetails(fieldValue: String): TransactionDetails? {
        try {
            val details = parseTransactionDetails(fieldValue)

            mapUsage(details)

            return details
        } catch (e: Exception) {
            log.error("Could not parse transaction details from field value '$fieldValue'", e)
        }

        return null
    }

    private fun parseTransactionDetails(fieldValue: String): TransactionDetails {
        val usage = StringBuilder("")
        val otherPartyName = StringBuilder("")
        var otherPartyBankCode: String? = null
        var otherPartyAccountId: String? = null
        var bookingText: String? = null
        var primaNotaNumber: String? = null
        var textKeySupplement: String? = null

        fieldValue.replace("\r", "").replace("\n", "").substring(3).split('?').forEach { subField ->
            if (subField.isNotEmpty()) {
                val fieldCode = subField.substring(0, 2).toInt()
                val fieldValue = subField.substring(2)

                when (fieldCode) {
                    0 -> bookingText = fieldValue
                    10 -> primaNotaNumber = fieldValue
                    in 20..29 -> usage.append(fieldValue)
                    30 -> otherPartyBankCode = fieldValue
                    31 -> otherPartyAccountId = fieldValue
                    32, 33 -> otherPartyName.append(fieldValue)
                    34 -> textKeySupplement = fieldValue
                    in 60..63 -> usage.append(fieldValue)
                }
            }
        }

        val details = TransactionDetails(
            usage.toString(), otherPartyName.toString(), otherPartyBankCode, otherPartyAccountId,
            bookingText, primaNotaNumber, textKeySupplement
        )
        return details
    }

    /**
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
     *
     * Weitere 4 Verwendungszwecke können zu den Feldschlüsseln 60 bis 63 eingestellt werden.
     */
    protected open fun mapUsage(details: TransactionDetails) {
        val usageParts = getUsageParts(details)

        usageParts.forEach { pair ->
            setUsageLineValue(details, pair.first, pair.second)
        }
    }

    private fun getUsageParts(details: TransactionDetails): MutableList<Pair<String, String>> {
        val usage = details.usage
        var previousMatchType = ""
        var previousMatchEnd = 0

        val usageParts = mutableListOf<Pair<String, String>>()
        val matcher = UsageTypePattern.matcher(details.usage)

        while (matcher.find()) {
            if (previousMatchEnd > 0) {
                val typeValue = usage.substring(previousMatchEnd, matcher.start())

                usageParts.add(Pair(previousMatchType, typeValue))
            }

            previousMatchType = usage.substring(matcher.start(), matcher.end())
            previousMatchEnd = matcher.end()
        }

        if (previousMatchEnd > 0) {
            val typeValue = usage.substring(previousMatchEnd, usage.length)

            usageParts.add(Pair(previousMatchType, typeValue))
        }

        return usageParts
    }

    protected open fun setUsageLineValue(details: TransactionDetails, usageType: String, typeValue: String) {
        when (usageType) {
            "EREF+" -> details.endToEndReference = typeValue
            "KREF+" -> details.customerReference = typeValue
            "MREF+" -> details.mandateReference = typeValue
            "CRED+" -> details.creditorIdentifier = typeValue
            "DEBT+" -> details.originatorsIdentificationCode = typeValue
            "COAM+" -> details.compensationAmount = typeValue
            "OAMT+" -> details.originalAmount = typeValue
            "SVWZ+" -> details.sepaUsage = typeValue
            "ABWA+" -> details.deviantOriginator = typeValue
            "ABWE+" -> details.deviantRecipient = typeValue
            else -> details.usageWithNoSpecialType = typeValue
        }
    }


    protected open fun parseDate(dateString: String): Date {
        return DateFormat.parse(dateString)
    }

    protected open fun parseAmount(amountString: String): BigDecimal {
        return amountString.replace(',', '.').toBigDecimal()
    }

}
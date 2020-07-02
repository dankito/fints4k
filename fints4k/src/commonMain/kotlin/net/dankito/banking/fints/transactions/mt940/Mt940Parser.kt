package net.dankito.banking.fints.transactions.mt940

import com.soywiz.klock.*
import net.dankito.banking.fints.model.Amount
import net.dankito.banking.fints.transactions.mt940.model.*
import net.dankito.banking.fints.util.log.LoggerFactory


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
        val AccountStatementsSeparatorRegex = Regex("^\\s*-\\s*\$", RegexOption.MULTILINE) // a line only with '-' and may other white space characters

        // (?<!T\d\d(:\d\d)?) to filter that date time with format (yyyy-MM-dd)Thh:mm:ss(:SSS) is considered to be a field identifier
        val AccountStatementFieldSeparatorRegex = Regex("(?<!T\\d\\d(:\\d\\d)?):\\d\\d\\w?:")


        const val TransactionReferenceNumberCode = "20"

        const val RelatedReferenceNumberCode = "21"

        const val AccountIdentificationCode = "25"

        const val StatementNumberCode = "28C"

        const val OpeningBalanceCode = "60"

        const val StatementLineCode = "61"

        const val InformationToAccountOwnerCode = "86"

        const val ClosingBalanceCode = "62"


        val DateFormat: DateFormat = DateFormat("yyMMdd")

        val CurrentYearTwoDigit = DateTime.now().yearInt

        val CreditDebitCancellationRegex = Regex("C|D|RC|RD")

        val AmountRegex = Regex("\\d+,\\d*")

        val UsageTypeRegex = Regex("[A-Z]{4}\\+")


        const val EndToEndReferenceUsageKey = "EREF+"
        const val CustomerReferenceUsageKey = "KREF+"
        const val MandateReferenceUsageKey = "MREF+"
        const val CreditorIdentifierUsageKey = "CRED+"
        const val OriginatorsIdentificationCodeUsageKey = "DEBT+"
        const val CompensationAmountUsageKey = "COAM+"
        const val OriginalAmountUsageKey = "OAMT+"
        const val SepaUsageUsageKey = "SVWZ+"
        const val DeviantOriginatorUsageKey = "ABWA+"
        const val DeviantRecipientUsageKey = "ABWE+"


        private val log = LoggerFactory.getLogger(Mt940Parser::class)
    }


    /**
     * Parses a whole MT 940 statements string, that is one that ends with a "-" line.
     */
    override fun parseMt940String(mt940String: String): List<AccountStatement> {
        return parseMt940Chunk(mt940String).first
    }

    /**
     * Parses incomplete MT 940 statements string, that is ones that not end with a "-" line,
     * as the they are returned e.g. if a HKKAZ response is dispersed over multiple messages.
     *
     * Tries to parse all statements in the string except an incomplete last one and returns an
     * incomplete last MT 940 statement (if any) as remainder.
     *
     * So each single HKKAZ partial response can be parsed immediately, its statements/transactions
     * be displayed immediately to user and the remainder then be passed together with next partial
     * HKKAZ response to this method till this whole MT 940 statement is parsed.
     */
    override fun parseMt940Chunk(mt940Chunk: String): Pair<List<AccountStatement>, String> {
        try {
            val singleAccountStatementsStrings = splitIntoSingleAccountStatements(mt940Chunk).toMutableList()

            var remainder = ""
            if (singleAccountStatementsStrings.isNotEmpty() && singleAccountStatementsStrings.last().isEmpty() == false) {
                remainder = singleAccountStatementsStrings.removeAt(singleAccountStatementsStrings.lastIndex)
            }

            val transactions = singleAccountStatementsStrings.mapNotNull { parseAccountStatement(it) }

            return Pair(transactions, remainder)
        } catch (e: Exception) {
            log.error(e) { "Could not parse account statements from MT940 string:\n$mt940Chunk" }
        }

        return Pair(listOf(), "")
    }


    protected open fun splitIntoSingleAccountStatements(mt940String: String): List<String> {
        return mt940String.split(AccountStatementsSeparatorRegex)
                        .map { it.replace("\n", "").replace("\r", "") }
    }


    protected open fun parseAccountStatement(accountStatementString: String): AccountStatement? {
        if (accountStatementString.isBlank()) {
            return null
        }

        try {
            val fieldsByCode = splitIntoFields(accountStatementString)

            return parseAccountStatement(fieldsByCode)
        } catch (e: Exception) {
            log.error(e) { "Could not parse account statement:\n$accountStatementString" }
        }

        return null
    }

    protected open fun splitIntoFields(accountStatementString: String): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        var lastMatchEnd = 0
        var lastMatchedCode = ""

        AccountStatementFieldSeparatorRegex.findAll(accountStatementString).forEach { matchResult ->
            if (lastMatchEnd > 0) {
                val previousStatement = accountStatementString.substring(lastMatchEnd, matchResult.range.first)
                result.add(Pair(lastMatchedCode, previousStatement))
            }

            lastMatchedCode = matchResult.value.replace(":", "")
            lastMatchEnd = matchResult.range.last + 1
        }

        if (lastMatchEnd > 0) {
            val previousStatement = accountStatementString.substring(lastMatchEnd, accountStatementString.length)
            result.add(Pair(lastMatchedCode, previousStatement))
        }

        return result
    }

    protected open fun parseAccountStatement(fieldsByCode: List<Pair<String, String>>): AccountStatement? {
        val statementAndMaySequenceNumber = getFieldValue(fieldsByCode, StatementNumberCode).split('/')
        val accountIdentification = getFieldValue(fieldsByCode, AccountIdentificationCode).split('/')
        val openingBalancePair = fieldsByCode.first { it.first.startsWith(OpeningBalanceCode) }
        val closingBalancePair = fieldsByCode.first { it.first.startsWith(ClosingBalanceCode) }

        return AccountStatement(
            getFieldValue(fieldsByCode, TransactionReferenceNumberCode),
            getOptionalFieldValue(fieldsByCode, RelatedReferenceNumberCode),
            accountIdentification[0],
            if (accountIdentification.size > 1) accountIdentification[1] else null,
            statementAndMaySequenceNumber[0].toInt(),
            if (statementAndMaySequenceNumber.size > 1) statementAndMaySequenceNumber[1].toInt() else null,
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

    protected open fun parseBalance(code: String, fieldValue: String): Balance {
        val isIntermediate = code.endsWith("M")

        val isDebit = fieldValue.startsWith("D")
        val bookingDateString = fieldValue.substring(1, 7)
        val statementDate = parseMt940Date(bookingDateString)
        val currency = fieldValue.substring(7, 10)
        val amountString = fieldValue.substring(10)
        val amount = parseAmount(amountString)

        return Balance(isIntermediate, !!!isDebit, statementDate, currency, amount)
    }

    protected open fun parseAccountStatementTransactions(fieldsByCode: List<Pair<String, String>>): List<Transaction> {
        val transactions = mutableListOf<Transaction>()

        fieldsByCode.forEachIndexed { index, pair ->
            if (pair.first == StatementLineCode) {
                val statementLine = parseStatementLine(pair.second)

                val nextPair = if (index < fieldsByCode.size - 1) fieldsByCode.get(index + 1) else null
                val information = if (nextPair?.first == InformationToAccountOwnerCode) parseNullableInformationToAccountOwner(nextPair.second) else null

                transactions.add(Transaction(statementLine, information))
            }
        }

        return transactions
    }

    /**
     * FORMAT
     * 6!n[4!n]2a[1!a]15d1!a3!c16x[//16x]
     * [34x]
     *
     * where subfields are:
     * Subfield   Format    Name
     *   1          6!n     (Value Date)
     *   2          [4!n]   (Entry Date)
     *   3          2a      (Debit/Credit Mark)
     *   4          [1!a]   (Funds Code)
     *   5          15d     (Amount)
     *   6          1!a3!c  (Transaction Type)(Identification Code)
     *   7          16x     (Reference for the Account Owner)
     *   8          [//16x] (Reference of the Account Servicing Institution)
     *   9          [34x]   (Supplementary Details)
     */
    protected open fun parseStatementLine(fieldValue: String): StatementLine {
        val valueDateString = fieldValue.substring(0, 6)
        val valueDate = parseMt940Date(valueDateString)

        val creditMarkMatchResult = CreditDebitCancellationRegex.find(fieldValue)
        val isDebit = creditMarkMatchResult?.value?.endsWith('D') == true
        val isCancellation = creditMarkMatchResult?.value?.startsWith('R') == true

        val creditMarkEnd = (creditMarkMatchResult?.range?.last ?: -1) + 1

        // booking date is the second field and is optional. It is normally only used when different from the value date.
        val bookingDateString = if ((creditMarkMatchResult?.range?.start ?: 0) > 6) fieldValue.substring(6, 10) else null
        val bookingDate = bookingDateString?.let { // bookingDateString has format MMdd -> add year from valueDateString
            parseMt940BookingDate(bookingDateString, valueDateString, valueDate)
        } ?: valueDate

        val amountMatchResult = AmountRegex.find(fieldValue)!!
        val amountString = amountMatchResult.value
        val amount = parseAmount(amountString)

        val amountEndIndex = amountMatchResult.range.last + 1

        val fundsCode = if (amountMatchResult.range.start - creditMarkEnd > 1) fieldValue.substring(creditMarkEnd + 1, creditMarkEnd + 2) else null

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

        val customerAndBankReference = fieldValue.substring(bookingKeyStart + 3).split("//")
        val customerReference = customerAndBankReference[0]

        /**
         * The content of this subfield is the account servicing institution's own reference for the transaction.
         * When the transaction has been initiated by the account servicing institution, this
         * reference may be identical to subfield 7, Reference for the Account Owner. If this is
         * the case, Reference of the Account Servicing Institution, subfield 8 may be omitted.
         */
        var bankReference = if (customerAndBankReference.size > 1) customerAndBankReference[1] else customerReference // TODO: or use null?
        var supplementaryDetails: String? = null

        val bankReferenceAndSupplementaryDetails = bankReference.split("\n")
        if (bankReferenceAndSupplementaryDetails.size > 1) {
            bankReference = bankReferenceAndSupplementaryDetails[0].trim()
            // TODO: parse /OCMT/ and /CHGS/, see page 518
            supplementaryDetails = bankReferenceAndSupplementaryDetails[1].trim()
        }

        return StatementLine(!!!isDebit, isCancellation, valueDate, bookingDate, null, amount, bookingKey,
            customerReference, bankReference, supplementaryDetails)
    }

    protected open fun parseNullableInformationToAccountOwner(informationToAccountOwnerString: String): InformationToAccountOwner? {
        try {
            val information = parseInformationToAccountOwner(informationToAccountOwnerString)

            mapUsage(information)

            return information
        } catch (e: Exception) {
            log.error(e) { "Could not parse InformationToAccountOwner from field value '$informationToAccountOwnerString'" }
        }

        return null
    }

    protected open fun parseInformationToAccountOwner(informationToAccountOwnerString: String): InformationToAccountOwner {
        // e. g. starts with 0 -> Inlandszahlungsverkehr, starts with '3' -> Wertpapiergeschäft
        // see Finanzdatenformate p. 209 - 215
        val geschaeftsvorfallCode = informationToAccountOwnerString.substring(0, 2) // TODO: may map

        val usageParts = mutableListOf<String>()
        val otherPartyName = StringBuilder()
        var otherPartyBankCode: String? = null
        var otherPartyAccountId: String? = null
        var bookingText: String? = null
        var primaNotaNumber: String? = null
        var textKeySupplement: String? = null

        informationToAccountOwnerString.substring(3).split('?').forEach { subField ->
            if (subField.isNotEmpty()) {
                val fieldCode = subField.substring(0, 2).toInt()
                val fieldValue = subField.substring(2)

                when (fieldCode) {
                    0 -> bookingText = fieldValue
                    10 -> primaNotaNumber = fieldValue
                    in 20..29 -> usageParts.add(fieldValue)
                    30 -> otherPartyBankCode = fieldValue
                    31 -> otherPartyAccountId = fieldValue
                    32, 33 -> otherPartyName.append(fieldValue)
                    34 -> textKeySupplement = fieldValue
                    in 60..63 -> usageParts.add(fieldValue)
                }
            }
        }

        val usage = if (isFormattedUsage(usageParts)) usageParts.joinToString("")
                    else usageParts.joinToString(" ")

        val otherPartyNameString = if (otherPartyName.isEmpty()) null else otherPartyName.toString()

        return InformationToAccountOwner(
            usage, otherPartyNameString, otherPartyBankCode, otherPartyAccountId,
            bookingText, primaNotaNumber, textKeySupplement
        )
    }

    protected open fun isFormattedUsage(usageParts: List<String>): Boolean {
        return usageParts.any { UsageTypeRegex.matches(it) }
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
    protected open fun mapUsage(information: InformationToAccountOwner) {
        val usageParts = getUsageParts(information.unparsedUsage)

        usageParts.forEach { entry ->
            setUsageLineValue(information, entry.key, entry.value)
        }
    }

    open fun getUsageParts(unparsedUsage: String): Map<String, String> {
        var previousMatchType = ""
        var previousMatchEnd = 0

        val usageParts = mutableMapOf<String, String>()

        UsageTypeRegex.findAll(unparsedUsage).forEach { matchResult ->
            if (previousMatchEnd > 0) {
                val typeValue = unparsedUsage.substring(previousMatchEnd, matchResult.range.first)

                usageParts[previousMatchType] = typeValue
            }

            previousMatchType = unparsedUsage.substring(matchResult.range)
            previousMatchEnd = matchResult.range.last + 1
        }

        if (previousMatchEnd > 0) {
            val typeValue = unparsedUsage.substring(previousMatchEnd, unparsedUsage.length)

            usageParts[previousMatchType] = typeValue
        }

        return usageParts
    }

    protected open fun setUsageLineValue(information: InformationToAccountOwner, usageType: String, typeValue: String) {
        when (usageType) {
            EndToEndReferenceUsageKey -> information.endToEndReference = typeValue
            CustomerReferenceUsageKey -> information.customerReference = typeValue
            MandateReferenceUsageKey -> information.mandateReference = typeValue
            CreditorIdentifierUsageKey -> information.creditorIdentifier = typeValue
            OriginatorsIdentificationCodeUsageKey -> information.originatorsIdentificationCode = typeValue
            CompensationAmountUsageKey -> information.compensationAmount = typeValue
            OriginalAmountUsageKey -> information.originalAmount = typeValue
            SepaUsageUsageKey -> information.sepaUsage = typeValue
            DeviantOriginatorUsageKey -> information.deviantOriginator = typeValue
            DeviantRecipientUsageKey -> information.deviantRecipient = typeValue
            else -> information.usageWithNoSpecialType = typeValue
        }
    }


    protected open fun parseMt940Date(dateString: String): Date {
        // TODO: this should be necessary anymore, isn't it?

        // SimpleDateFormat is not thread-safe. Before adding another library i decided to parse
        // this really simple date format on my own
        if (dateString.length == 6) {
            try {
                var year = dateString.substring(0, 2).toInt() + 2000
                val month = dateString.substring(2, 4).toInt()
                val day = dateString.substring(4, 6).toInt()

                if (year > CurrentYearTwoDigit + 1) { // should be rarely the case: years before 2000
                    year -= 100
                }

                return Date(year, month, day) // java.util.Date years start at 1900 at month at 0 not at 1
            } catch (e: Exception) {
                log.error(e) { "Could not parse dateString '$dateString'" }
            }
        }

        return DateFormat.parse(dateString).utc.date // fallback to not thread-safe SimpleDateFormat. Works in most cases but not all
    }

    /**
     * Booking date string consists only of MMDD -> we need to take the year from value date string.
     */
    protected open fun parseMt940BookingDate(bookingDateString: String, valueDateString: String, valueDate: Date): Date {
        val bookingDate = parseMt940Date(valueDateString.substring(0, 2) + bookingDateString)

        // there are rare cases that booking date is e.g. on 31.12.2019 and value date on 01.01.2020 -> booking date would be on 31.12.2020 (and therefore in the future)
        if (bookingDate.month != valueDate.month && bookingDate.month == Month.December) {
            return parseMt940Date("" + (valueDate.year - 1 - 2000) + bookingDateString)
        }

        return bookingDate
    }

    protected open fun parseAmount(amountString: String): Amount {
        return Amount(amountString)
    }

}
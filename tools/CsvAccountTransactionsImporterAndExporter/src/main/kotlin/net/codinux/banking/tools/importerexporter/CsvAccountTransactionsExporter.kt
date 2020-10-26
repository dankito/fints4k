package net.codinux.banking.tools.importerexporter

import net.codinux.banking.tools.importerexporter.model.AccountTransaction
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.LoggerFactory
import java.io.Writer
import java.math.BigDecimal
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*


open class CsvAccountTransactionsExporter : IAccountTransactionsExporter {

    companion object {

        // TODO: translate
        val Headers = listOf(
            "Auftragskonto", "Buchungstag", "Wertstellungstag", "Umsatzart", "Empfänger / Auftraggeber",
            "Verwendungszweck", "IBAN/Kontonummer", "BIC", "Umsatz", "Währung"
        )

        const val DateSeparators = "[./ -]"
        val DateOnlyContainsNumbersRegex = Regex("\\d{1,4}$DateSeparators\\d{1,2}$DateSeparators\\d{2,4}")

        private val log = LoggerFactory.getLogger(CsvAccountTransactionsExporter::class.java)
    }


    // set as fields not as companion object members so they use the Locale set at CsvAccountTransactionsExporter instantiation time not when the first CsvAccountTransactionsExporter has been created

    protected open val DateFormatter: DateFormat = findLongestDateFormatWithoutWrittenOutMonth() // ensure converted dates only contain numbers, not dates like 27 Mar 2020

    protected open val DecimalFormat = NumberFormat.getNumberInstance()


    override fun export(writer: Writer, transactions: Collection<AccountTransaction>) {
        try {
            writer.use {
                val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(*Headers.toTypedArray()))

                transactions.forEach { transaction ->
                    csvPrinter.printRecord(transaction.account, format(transaction.bookingDate), format(transaction.valueDate),
                        format(transaction.bookingText), format(transaction.otherPartyName), transaction.reference,
                        format(transaction.otherPartyAccountId), format(transaction.otherPartyBankCode), format(transaction.amount), transaction.currency
                    )
                }

                csvPrinter.flush()
            }
        } catch (e: Exception) {
            log.error("Could not export ${transactions.size} transactions to CSV", e)
        }
    }


    protected open fun format(date: Date): String {
        return DateFormatter.format(date)
    }

    protected open fun format(bigDecimal: BigDecimal): String {
        return DecimalFormat.format(bigDecimal)
    }

    protected open fun format(string: String?): String {
        return string ?: ""
    }


    protected open fun findLongestDateFormatWithoutWrittenOutMonth(): DateFormat {
        val mediumDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
        val dateFormatTest = mediumDateFormat.format(Date())

        return if (DateOnlyContainsNumbersRegex.matches(dateFormatTest)) {
            mediumDateFormat
        }
        else {
            DateFormat.getDateInstance(DateFormat.SHORT)
        }
    }

}
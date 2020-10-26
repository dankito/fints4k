package net.codinux.banking.tools.importerexporter

import net.codinux.banking.tools.importerexporter.model.AccountTransaction
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.awt.print.Book
import java.io.StringWriter
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ThreadLocalRandom

internal class CsvAccountTransactionsExporterTest {

    companion object {
        const val AccountId = "DE00876543210123456789"

        const val Currency = "EUR"

        val BookingDateDay = 26 // one day before value date
        val ValueDateDay = 27
        val DateMonth = 3
        val DateYear = 1988

        val BookingDate = Date(DateYear - 1900, DateMonth - 1, BookingDateDay)

        val ValueDate = Date(DateYear - 1900, DateMonth - 1, ValueDateDay)

        const val ReferenceWithUmlaute = "Was für ein schöner Verwendungszweck!"

        const val AsciiReference = "A total normal reference"

        const val Amount1String = "84.23"
        val Amount1 = BigDecimal(Amount1String)

        const val OtherParty1Name = "Nelson Mandela"
        const val OtherParty1BankCode = "ABCDEFGH123"
        const val OtherParty1AccountId = "SA99012345679876543210"

        const val BookingText1 = "Überweisung"

        const val Amount2String = "-123.45"
        val Amount2 = BigDecimal(Amount2String)

        val OtherParty2Name: String? = null
        val OtherParty2BankCode: String? = null
        val OtherParty2AccountId : String? = null

        const val BookingText2 = "Bargeldabhebung"
    }


    private val defaultLocale = Locale.getDefault(Locale.Category.FORMAT)


    @AfterEach
    fun tearDown() {
        setLocale(defaultLocale) // restore locale
    }


    @Test
    fun exportWithEnglishLocale() {
        setLocale(Locale.US)

        val underTest = CsvAccountTransactionsExporter() // has to be created after locale is set as otherwise DateFormat works with the wrong locale

        val transactions = createTransactions(2)

        val writer = StringWriter()

        underTest.export(writer, transactions)


        val result = writer.toString()

        assertThat(result).contains(Amount1String)
        assertThat(result).contains(Amount2String)

        assertThat(result).contains("$DateMonth/$BookingDateDay/${DateYear - 1900}")
        assertThat(result).contains("$DateMonth/$ValueDateDay/${DateYear - 1900}")

        assertThat(result).contains(ReferenceWithUmlaute, AsciiReference)

//        assertThat(countOccurrences(result, ',')).isEqualTo(calculateCountSeparators(2)) // actually first thought using the German standard delimiter ';', but now sticking with ','
    }

    @Test
    fun exportWithGermanLocale() {
        setLocale(Locale.GERMANY)

        val underTest = CsvAccountTransactionsExporter() // has to be created after locale is set as otherwise DateFormat works with the wrong locale

        val transactions = createTransactions(2)

        val writer = StringWriter()

        underTest.export(writer, transactions)


        val result = writer.toString()

        assertThat(result).contains(Amount1String.replace('.', ','))
        assertThat(result).contains(Amount2String.replace('.', ','))

        assertThat(result).contains("$BookingDateDay.0$DateMonth.$DateYear")
        assertThat(result).contains("$ValueDateDay.0$DateMonth.$DateYear")

        assertThat(result).contains(ReferenceWithUmlaute, AsciiReference)

//        assertThat(countOccurrences(result, ',')).isEqualTo(calculateCountSeparators(2)) // actually first thought using the German standard delimiter ';', but now sticking with ','
    }


    private fun createTransactions(count: Int): List<AccountTransaction> {
        val transactions = mutableListOf<AccountTransaction>()

        IntRange(0, count - 1).forEach { index ->
            transactions.add(createTransaction(index))
        }

        return transactions
    }

    private fun createTransaction(index: Int): AccountTransaction {
        return when (index) {
            0 -> createTransaction(Amount1, ReferenceWithUmlaute, OtherParty1Name, OtherParty1BankCode, OtherParty1AccountId, BookingText1)
            1 -> createTransaction(Amount2, AsciiReference, OtherParty2Name, OtherParty2BankCode, OtherParty2AccountId, BookingText2)
            else -> createRandomTransaction()
        }
    }

    private fun createRandomTransaction(): AccountTransaction {
        val random = ThreadLocalRandom.current()

        val amount = random.nextDouble(-1_000_000.0, 1_000_000.0)

        return createTransaction(BigDecimal.valueOf(amount), "")
    }

    private fun createTransaction(amount: BigDecimal, reference: String, otherPartyName: String? = null, otherPartyBankCode: String? = null,
                                  otherPartyAccountId: String? = null, bookingText: String? = null): AccountTransaction {
        return AccountTransaction(
            AccountId, amount, Currency, reference, BookingDate, ValueDate, otherPartyName, otherPartyBankCode, otherPartyAccountId, bookingText
        )
    }


    private fun countOccurrences(string: String, characterToFind: Char): Int {
        var countOccurrences = 0

        for (char in string) {
            if (char == characterToFind) {
                countOccurrences++
            }
        }

        return countOccurrences
    }

    private fun calculateCountSeparators(countTransactions: Int): Int {
        return (countTransactions + 1) * // + 1 cause of header row
                (10 - 1) // - 1 cause for the last column no separator gets printed
    }


    private fun setLocale(locale: Locale) {
        Locale.setDefault(Locale.Category.FORMAT, locale)
    }
}
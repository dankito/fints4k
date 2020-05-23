package net.dankito.banking.fints.transactions

import net.dankito.banking.fints.FinTsTestBase
import net.dankito.banking.fints.transactions.mt940.Mt940Parser
import net.dankito.banking.fints.transactions.mt940.model.Balance
import net.dankito.banking.fints.transactions.mt940.model.InformationToAccountOwner
import net.dankito.banking.fints.transactions.mt940.model.StatementLine
import net.dankito.utils.Stopwatch
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*


class Mt940ParserTest : FinTsTestBase() {

    companion object {

        const val Currency = "EUR"

        val AccountStatement1PreviousStatementBookingDate = Date(88, 2, 26)
        val AccountStatement1BookingDate = Date(88, 2, 27)

        val AccountStatement1OpeningBalanceAmount = 12345.67.toBigDecimal()

        val AccountStatement1Transaction1Amount = 1234.56.toBigDecimal()
        val AccountStatement1Transaction1OtherPartyName = "Sender1"
        val AccountStatement1Transaction1OtherPartyBankCode = "AAAADE12"
        val AccountStatement1Transaction1OtherPartyAccountId = "DE99876543210987654321"

        val AccountStatement1Transaction2Amount = 432.10.toBigDecimal()
        val AccountStatement1Transaction2OtherPartyName = "Receiver2"
        val AccountStatement1Transaction2OtherPartyBankCode = "BBBBDE56"
        val AccountStatement1Transaction2OtherPartyAccountId = "DE77987654321234567890"

        val AccountStatement1ClosingBalanceAmount = AccountStatement1OpeningBalanceAmount + AccountStatement1Transaction1Amount
        val AccountStatement1With2TransactionsClosingBalanceAmount = AccountStatement1OpeningBalanceAmount + AccountStatement1Transaction1Amount - AccountStatement1Transaction2Amount
    }

    private val underTest = Mt940Parser()


    @Test
    fun accountStatementWithSingleTransaction() {

        // when
        val result = underTest.parseMt940String(AccountStatementWithSingleTransaction)


        // then
        assertThat(result).hasSize(1)

        val statement = result.first()

        assertThat(statement.bankCodeBicOrIban).isEqualTo(BankCode)
        assertThat(statement.accountIdentifier).isEqualTo(CustomerId)
        assertBalance(statement.openingBalance, true, AccountStatement1PreviousStatementBookingDate, AccountStatement1OpeningBalanceAmount)
        assertBalance(statement.closingBalance, true, AccountStatement1BookingDate, AccountStatement1ClosingBalanceAmount)

        assertThat(statement.transactions).hasSize(1)

        val transaction = statement.transactions.first()
        assertTurnover(transaction.statementLine, AccountStatement1BookingDate, AccountStatement1Transaction1Amount)
        assertTransactionDetails(transaction.information, AccountStatement1Transaction1OtherPartyName,
            AccountStatement1Transaction1OtherPartyBankCode, AccountStatement1Transaction1OtherPartyAccountId)
    }

    @Test
    fun accountStatementWithSingleTransaction_SheetNumberOmitted() {

        // given
        val amount = BigDecimal("15.00")
        val isCredit = false
        val bookingDate = Date(120, 4, 11)

        // when
        val result = underTest.parseMt940String(AccountStatementWithSingleTransaction_SheetNumberOmitted)


        // then
        assertThat(result).hasSize(1)

        val statement = result.first()

        assertThat(statement.bankCodeBicOrIban).isEqualTo(BankCode)
        assertThat(statement.accountIdentifier).isEqualTo(CustomerId)
        assertThat(statement.statementNumber).isEqualTo(0)
        assertThat(statement.sequenceNumber).isNull()

        assertBalance(statement.openingBalance, true, bookingDate, BigDecimal("0.00"))
        assertBalance(statement.closingBalance, isCredit, bookingDate, amount)

        assertThat(statement.transactions).hasSize(1)

        val transaction = statement.transactions.first()
        assertTurnover(transaction.statementLine, bookingDate, amount, isCredit)
        assertTransactionDetails(transaction.information, "Ausgabe einer Debitkarte", BankCode, CustomerId)
    }

    @Test
    fun accountStatementWithTwoTransactions() {

        // when
        val result = underTest.parseMt940String(AccountStatementWithTwoTransactions)


        // then
        assertThat(result).hasSize(1)

        val statement = result.first()

        assertThat(statement.bankCodeBicOrIban).isEqualTo(BankCode)
        assertThat(statement.accountIdentifier).isEqualTo(CustomerId)
        assertBalance(statement.openingBalance, true, AccountStatement1PreviousStatementBookingDate, AccountStatement1OpeningBalanceAmount)
        assertBalance(statement.closingBalance, true, AccountStatement1BookingDate, AccountStatement1With2TransactionsClosingBalanceAmount)

        assertThat(statement.transactions).hasSize(2)

        val firstTransaction = statement.transactions.first()
        assertTurnover(firstTransaction.statementLine, AccountStatement1BookingDate, AccountStatement1Transaction1Amount)
        assertTransactionDetails(firstTransaction.information, AccountStatement1Transaction1OtherPartyName,
            AccountStatement1Transaction1OtherPartyBankCode, AccountStatement1Transaction1OtherPartyAccountId)

        val secondTransaction = statement.transactions[1]
        assertTurnover(secondTransaction.statementLine, AccountStatement1BookingDate, AccountStatement1Transaction2Amount, false)
        assertTransactionDetails(secondTransaction.information, AccountStatement1Transaction2OtherPartyName,
            AccountStatement1Transaction2OtherPartyBankCode, AccountStatement1Transaction2OtherPartyAccountId)
    }

    @Test
    fun `Fix annual jump from booking date to value date`() {

        val transactionsString = ":20:STARTUMSE\n" +
                ":25:72051210/0560165557\n" +
                ":28C:00000/001\n" +
                ":60F:C191227EUR104501,86\n" +
                ":61:2001011230DR3,99N024NONREF\n" +
                ":86:809?00ENTGELTABSCHLUSS?106666?20Entgeltabrechnung?21siehe Anl\n" +
                "age?3072051210\n" +
                ":61:2001011230CR0,00N066NONREF\n" +
                ":86:805?00ABSCHLUSS?106666?20Abrechnung 30.12.2019?21siehe Anlage\n" +
                "?3072051210\n" +
                ":62F:C191230EUR104490,88\n" +
                "-"


        // when
        val result = underTest.parseMt940String(transactionsString)


        // then
        assertThat(result).hasSize(1)
        assertThat(result.first().transactions).hasSize(2)

        assertThat(result.first().transactions[0].statementLine.bookingDate).isEqualTo(Date(119, 11, 30))
        assertThat(result.first().transactions[0].statementLine.valueDate).isEqualTo(Date(120, 0, 1))
        assertThat(result.first().transactions[1].statementLine.bookingDate).isEqualTo(Date(119, 11, 30))
        assertThat(result.first().transactions[1].statementLine.valueDate).isEqualTo(Date(120, 0, 1))
    }

    @Test
    fun `Fix line starts with dash but is not a statement separator`() {

        // given
        val transactionsString = "\n" +
                ":20:MT940-2005200849\n" +
                ":21:NONREF\n" +
                ":25:20041111/369300900EUR\n" +
                ":28C:0/1\n" +
                ":60F:C200512EUR0,00\n" +
                ":61:2005120512CR100,00NMSCNONREF//POS 7\n" +
                ":86:820?20ÜBERTRAG / ÜBERWEISUNG?21EROEFFNUNGSBETRAG?22END-TO-END-REF\n" +
                ".:?23NICHT ANGEGEBEN?24Ref. HW220133C3232360/15499?32DAN\n" +
                "NKITO\n" +
                ":62M:C200513EUR100,00\n" +
                "-\n" +
                ":20:MT940-2005200849\n" +
                ":21:NONREF\n" +
                ":25:20041111/369300900EUR\n" +
                ":28C:0/2\n" +
                ":60M:C200513EUR100,00\n" +
                ":61:2005130513CR0,10NMSCNONREF//POS 6\n" +
                ":86:820?20ÜBERTRAG / ÜBERWEISUNG?21TEST?22END-TO-END-REF.:?23NICHT AN\n" +
                "GEGEBEN?24Ref. 7T2C0YTD0BZL4V9S/1?32DANKITO\n" +
                ":61:2005130513CR0,15NMSCNONREF//POS 5\n" +
                ":86:820?20ÜBERTRAG / ÜBERWEISUNG?21ECHTZEITUEBERWEISUNGSTEST?22END-TO\n" +
                "-END-REF.:?23NICHT ANGEGEBEN?24Ref. 402C0YTD0GLPFDFV/1?32DANKI\n" +
                "TO\n" +
                ":61:2005130513CR0,30NMSCNONREF//POS 4\n" +
                ":86:820?20ÜBERTRAG / ÜBERWEISUNG?21UND NOCH EIN TEST FUER JAVA?22FX?2\n" +
                "3END-TO-END-REF.:?24NICHT ANGEGEBEN?25Ref. 5D2C0YTD0HVAB3X3/1?32D\n" +
                "ANKITO\n" +
                ":61:2005130513CR0,10NMSCNONREF//POS 3\n" +
                ":86:820?20ÜBERTRAG / ÜBERWEISUNG?21LASS DIE KOHLE RUEBER WACHS?22EN?2\n" +
                "3END-TO-END-REF.:?24NICHT ANGEGEBEN?25Ref. J3220134C3451151/6200?\n" +
                "32DANKITO\n" +
                ":61:2005130513CR0,01NMSCNONREF//POS 2\n" +
                ":86:820?20ÜBERTRAG / ÜBERWEISUNG?21TEST?22END-TO-END-REF.:?23NICHT AN\n" +
                "GEGEBEN?24Ref. J3220134C3451151/6201?32DANKITO\n" +
                ":62M:C200514EUR100,66\n" +
                "-\n" +
                ":20:MT940-2005200849\n" +
                ":21:NONREF\n" +
                ":25:20041111/369300900EUR\n" +
                ":28C:0/3\n" +
                ":60M:C200514EUR100,66\n" +
                ":61:2005140514DR0,01NMSCNONREF//POS 1\n" +
                ":86:820?20ÜBERTRAG / ÜBERWEISUNG?21END-TO-END-REF.:?22NICHT ANGEGEBEN\n" +
                "?23Ref. J022013510234936/2?30ABCDEFGHIJK?31DE1112345679876543210\n" +
                "?32DANKITO\n" +
                ":62F:C200520EUR100,65\n" +
                "-"


        // when
        val result = underTest.parseMt940String(transactionsString)


        // then
        assertThat(result).hasSize(3)
        assertThat(result.flatMap { it.transactions }).hasSize(7)
    }

    @Test
    fun parseTransactions() {

        // given
        val transactionsString = loadTestFile(TransactionsMt940Filename)


        // when
        val result = underTest.parseMt940String(transactionsString)


        // then
        assertThat(result).hasSize(32)
    }


    private fun assertBalance(balance: Balance, isCredit: Boolean, bookingDate: Date, amount: BigDecimal) {
        assertThat(balance.isCredit).isEqualTo(isCredit)
        assertThat(balance.bookingDate).isEqualTo(bookingDate)
        assertThat(balance.amount).isEqualTo(amount)
        assertThat(balance.currency).isEqualTo(Currency)
    }

    private fun assertTurnover(statementLine: StatementLine, valueDate: Date, amount: BigDecimal, isCredit: Boolean = true,
                               bookingDate: Date? = valueDate) {

        assertThat(statementLine.isCredit).isEqualTo(isCredit)
        assertThat(statementLine.isCancellation).isFalse()
        assertThat(statementLine.valueDate).isEqualTo(valueDate)
        assertThat(statementLine.bookingDate).isEqualTo(bookingDate)
        assertThat(statementLine.amount).isEqualTo(amount)
    }

    private fun assertTransactionDetails(details: InformationToAccountOwner?, otherPartyName: String,
                                         otherPartyBankCode: String, otherPartyAccountId: String) {

        assertThat(details).isNotNull()

        assertThat(details?.otherPartyName).isEqualTo(otherPartyName)
        assertThat(details?.otherPartyBankCode).isEqualTo(otherPartyBankCode)
        assertThat(details?.otherPartyAccountId).isEqualTo(otherPartyAccountId)
    }


    private val AccountStatementWithSingleTransaction = """
        :20:STARTUMSE
        :25:$BankCode/$CustomerId
        :28C:00000/001
        :60F:C${convertMt940Date(AccountStatement1PreviousStatementBookingDate)}EUR${convertAmount(AccountStatement1OpeningBalanceAmount)}
        :61:${convertMt940Date(AccountStatement1BookingDate)}${convertToShortBookingDate(AccountStatement1BookingDate)}CR${convertAmount(AccountStatement1Transaction1Amount)}N062NONREF
        :86:166?00GUTSCHR. UEBERWEISUNG?109249?20EREF+674?21SVWZ+1908301/
        EUR ${convertAmount(AccountStatement1Transaction1Amount)}/20?2219-10-02/...?30$AccountStatement1Transaction1OtherPartyBankCode?31$AccountStatement1Transaction1OtherPartyAccountId
        ?32$AccountStatement1Transaction1OtherPartyName
        :62F:C${convertMt940Date(AccountStatement1BookingDate)}EUR${convertAmount(AccountStatement1ClosingBalanceAmount)}
        -
    """.trimIndent()

    private val AccountStatementWithTwoTransactions = """
        :20:STARTUMSE
        :25:$BankCode/$CustomerId
        :28C:00000/001
        :60F:C${convertMt940Date(AccountStatement1PreviousStatementBookingDate)}EUR${convertAmount(AccountStatement1OpeningBalanceAmount)}
        :61:${convertMt940Date(AccountStatement1BookingDate)}${convertToShortBookingDate(AccountStatement1BookingDate)}CR${convertAmount(AccountStatement1Transaction1Amount)}N062NONREF
        :86:166?00GUTSCHR. UEBERWEISUNG?109249?20EREF+674?21SVWZ+1908301/
        EUR ${convertAmount(AccountStatement1Transaction1Amount)}/20?2219-10-02/...?30$AccountStatement1Transaction1OtherPartyBankCode?31$AccountStatement1Transaction1OtherPartyAccountId
        ?32$AccountStatement1Transaction1OtherPartyName
        :61:${convertMt940Date(AccountStatement1BookingDate)}${convertToShortBookingDate(AccountStatement1BookingDate)}DR${convertAmount(AccountStatement1Transaction2Amount)}N062NONREF
        :86:166?00ONLINE-UEBERWEISUNG?109249?20EREF+674?21SVWZ+1908301/
        EUR ${convertAmount(AccountStatement1Transaction2Amount)}/20?2219-10-02/...?30$AccountStatement1Transaction2OtherPartyBankCode?31$AccountStatement1Transaction2OtherPartyAccountId
        ?32$AccountStatement1Transaction2OtherPartyName
        :62F:C${convertMt940Date(AccountStatement1BookingDate)}EUR${convertAmount(AccountStatement1With2TransactionsClosingBalanceAmount)}
        -
    """.trimIndent()

    private val AccountStatementWithSingleTransaction_SheetNumberOmitted = """
        :20:STARTUMS
        :25:$BankCode/$CustomerId
        :28C:0
        :60F:C200511EUR0,00
        :61:200511D15,00NMSCNONREF
        :86:808?00Entgelt/Auslagen?10907?20Preis bezahlt bis 12.2020
        ?21Jahrespreis?22$AccountHolderName?23Folge-Nr.   0 Verfall 12.23
        ?30$BankCode?31$CustomerId?32Ausgabe einer Debitkarte
        :62F:D200511EUR15,00
        -
    """.trimIndent()


    private fun convertMt940Date(date: Date): String {
        return Mt940Parser.DateFormat.format(date)
    }

    private fun convertToShortBookingDate(date: Date): String {
        return SimpleDateFormat("MMdd").format(date)
    }

}
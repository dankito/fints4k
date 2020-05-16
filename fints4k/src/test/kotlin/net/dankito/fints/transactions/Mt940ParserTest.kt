package net.dankito.fints.transactions

import net.dankito.fints.FinTsTestBase
import net.dankito.fints.transactions.mt940.Mt940Parser
import net.dankito.fints.transactions.mt940.model.Balance
import net.dankito.fints.transactions.mt940.model.TransactionDetails
import net.dankito.fints.transactions.mt940.model.Turnover
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
        assertTurnover(transaction.turnover, AccountStatement1BookingDate, AccountStatement1Transaction1Amount)
        assertTransactionDetails(transaction.details, AccountStatement1Transaction1OtherPartyName,
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
        assertTurnover(transaction.turnover, bookingDate, amount, isCredit, null)
        assertTransactionDetails(transaction.details, "Ausgabe einer Debitkarte", BankCode, CustomerId)
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
        assertTurnover(firstTransaction.turnover, AccountStatement1BookingDate, AccountStatement1Transaction1Amount)
        assertTransactionDetails(firstTransaction.details, AccountStatement1Transaction1OtherPartyName,
            AccountStatement1Transaction1OtherPartyBankCode, AccountStatement1Transaction1OtherPartyAccountId)

        val secondTransaction = statement.transactions[1]
        assertTurnover(secondTransaction.turnover, AccountStatement1BookingDate, AccountStatement1Transaction2Amount, false)
        assertTransactionDetails(secondTransaction.details, AccountStatement1Transaction2OtherPartyName,
            AccountStatement1Transaction2OtherPartyBankCode, AccountStatement1Transaction2OtherPartyAccountId)
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

    private fun assertTurnover(turnover: Turnover, valueDate: Date, amount: BigDecimal, isCredit: Boolean = true,
                               bookingDate: Date? = valueDate) {

        assertThat(turnover.isCredit).isEqualTo(isCredit)
        assertThat(turnover.isCancellation).isFalse()
        assertThat(turnover.valueDate).isEqualTo(valueDate)
        assertThat(turnover.bookingDate).isEqualTo(bookingDate)
        assertThat(turnover.amount).isEqualTo(amount)
    }

    private fun assertTransactionDetails(details: TransactionDetails?, otherPartyName: String,
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
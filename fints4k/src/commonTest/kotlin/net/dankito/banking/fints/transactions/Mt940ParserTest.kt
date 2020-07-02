package net.dankito.banking.fints.transactions

import ch.tutteli.atrium.api.fluent.en_GB.hasSize
import ch.tutteli.atrium.api.fluent.en_GB.notToBeNull
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import net.dankito.banking.fints.FinTsTestBase
import net.dankito.banking.fints.transactions.mt940.Mt940Parser
import net.dankito.banking.fints.transactions.mt940.model.Balance
import net.dankito.banking.fints.transactions.mt940.model.InformationToAccountOwner
import net.dankito.banking.fints.transactions.mt940.model.StatementLine
import ch.tutteli.atrium.api.verbs.expect
import kotlin.test.Test
import com.soywiz.klock.Date
import com.soywiz.klock.DateFormat
import net.dankito.banking.fints.extensions.isFalse
import net.dankito.banking.fints.model.Amount


class Mt940ParserTest : FinTsTestBase() {

    companion object {

        const val Currency = "EUR"

        val AccountStatement1PreviousStatementBookingDate = Date(1988, 2, 26)
        val AccountStatement1BookingDate = Date(1988, 2, 27)

        val AccountStatement1OpeningBalanceAmount = Amount("12345,67")

        val AccountStatement1Transaction1Amount = Amount("1234,56")
        val AccountStatement1Transaction1OtherPartyName = "Sender1"
        val AccountStatement1Transaction1OtherPartyBankCode = "AAAADE12"
        val AccountStatement1Transaction1OtherPartyAccountId = "DE99876543210987654321"

        val AccountStatement1Transaction2Amount = Amount("432,10")
        val AccountStatement1Transaction2OtherPartyName = "Receiver2"
        val AccountStatement1Transaction2OtherPartyBankCode = "BBBBDE56"
        val AccountStatement1Transaction2OtherPartyAccountId = "DE77987654321234567890"

        val AccountStatement1ClosingBalanceAmount = Amount("13580,23")
        val AccountStatement1With2TransactionsClosingBalanceAmount = Amount("13148,13")
    }

    private val underTest = Mt940Parser()


    @Test
    fun accountStatementWithSingleTransaction() {

        // when
        val result = underTest.parseMt940String(AccountStatementWithSingleTransaction)


        // then
        expect(result).hasSize(1)

        val statement = result.first()

        expect(statement.bankCodeBicOrIban).toBe(BankCode)
        expect(statement.accountIdentifier).toBe(CustomerId)
        assertBalance(statement.openingBalance, true, AccountStatement1PreviousStatementBookingDate, AccountStatement1OpeningBalanceAmount)
        assertBalance(statement.closingBalance, true, AccountStatement1BookingDate, AccountStatement1ClosingBalanceAmount)

        expect(statement.transactions).hasSize(1)

        val transaction = statement.transactions.first()
        assertTurnover(transaction.statementLine, AccountStatement1BookingDate, AccountStatement1Transaction1Amount)
        assertTransactionDetails(transaction.information, AccountStatement1Transaction1OtherPartyName,
            AccountStatement1Transaction1OtherPartyBankCode, AccountStatement1Transaction1OtherPartyAccountId)
    }

    @Test
    fun accountStatementWithSingleTransaction_SheetNumberOmitted() {

        // given
        val amount = Amount("15,00")
        val isCredit = false
        val bookingDate = Date(2020, 5, 11)

        // when
        val result = underTest.parseMt940String(AccountStatementWithSingleTransaction_SheetNumberOmitted)


        // then
        expect(result).hasSize(1)

        val statement = result.first()

        expect(statement.bankCodeBicOrIban).toBe(BankCode)
        expect(statement.accountIdentifier).toBe(CustomerId)
        expect(statement.statementNumber).toBe(0)
        expect(statement.sequenceNumber).toBe(null)

        assertBalance(statement.openingBalance, true, bookingDate, Amount("0,00"))
        assertBalance(statement.closingBalance, isCredit, bookingDate, amount)

        expect(statement.transactions).hasSize(1)

        val transaction = statement.transactions.first()
        assertTurnover(transaction.statementLine, bookingDate, amount, isCredit)
        assertTransactionDetails(transaction.information, "Ausgabe einer Debitkarte", BankCode, CustomerId)
    }

    @Test
    fun accountStatementWithTwoTransactions() {

        // when
        val result = underTest.parseMt940String(AccountStatementWithTwoTransactions)


        // then
        expect(result).hasSize(1)

        val statement = result.first()

        expect(statement.bankCodeBicOrIban).toBe(BankCode)
        expect(statement.accountIdentifier).toBe(CustomerId)
        assertBalance(statement.openingBalance, true, AccountStatement1PreviousStatementBookingDate, AccountStatement1OpeningBalanceAmount)
        assertBalance(statement.closingBalance, true, AccountStatement1BookingDate, AccountStatement1With2TransactionsClosingBalanceAmount)

        expect(statement.transactions).hasSize(2)

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
                ":25:$BankCode/$CustomerId\n" +
                ":28C:00000/001\n" +
                ":60F:C191227EUR104501,86\n" +
                ":61:2001011230DR3,99N024NONREF\n" +
                ":86:809?00ENTGELTABSCHLUSS?106666?20Entgeltabrechnung?21siehe Anl\n" +
                "age?30$BankCode\n" +
                ":61:2001011230CR0,00N066NONREF\n" +
                ":86:805?00ABSCHLUSS?106666?20Abrechnung 30.12.2019?21siehe Anlage\n" +
                "?30$BankCode\n" +
                ":62F:C191230EUR104490,88\n" +
                "-"


        // when
        val result = underTest.parseMt940String(transactionsString)


        // then
        expect(result).hasSize(1)
        expect(result.first().transactions).hasSize(2)

        expect(result.first().transactions[0].statementLine.bookingDate).toBe(Date(2019, 12, 30))
        expect(result.first().transactions[0].statementLine.valueDate).toBe(Date(2020, 1, 1))
        expect(result.first().transactions[1].statementLine.bookingDate).toBe(Date(2019, 12, 30))
        expect(result.first().transactions[1].statementLine.valueDate).toBe(Date(2020, 1, 1))
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
        expect(result).hasSize(3)
        expect(result.flatMap { it.transactions }).hasSize(7)
    }

    @Test
    fun `Fix that time got detected as field code`() {

        // given
        val transactionsString = "\n" +
                ":20:STARTUMS\n" +
                ":25:$BankCode/$CustomerId\n" +
                ":28C:0\n" +
                ":60F:D200514EUR15,00\n" +
                ":61:200514D0,02NMSCKREF+\n" +
                ":86:177?00SEPA Überweisung?10804?20KREF+2020-05-14T00:58:23:09\n" +
                "?2193 ?22SVWZ+Test TAN1:Auftrag nich\n" +
                "?23t TAN-pflichtig IBAN: DE111?23456780987654321 BIC: ABCD\n" +
                "?25DEMM123 ?30$Bic?31$Iban\n" +
                "?32DANKITO\n" +
                ":61:200514D0,05NMSCKREF+\n" +
                ":86:177?00SEPA Überweisung?10804?20KREF+2020-05-14T01:35:20.67\n" +
                "?216 ?22SVWZ+Lass es endlich ruber?23wachsen TAN1:Auftrag nicht \n" +
                "?24TAN-pflichtig IBAN: DE11123?25456780987654321 BIC: ABCDDE\n" +
                "?26MM123 ?30$Bic?31$Iban\n" +
                "?32DANKITO\n" +
                ":61:200514C0,01NMSC\n" +
                ":86:166?00SEPA Gutschrift?10804?20SVWZ+2020-05-14T13:10:34.09\n" +
                "?211 Test transaction b0a557f2?22 f962-4608-9201-f890e1fc037\n" +
                "?23b IBAN: DE11123456780987654?24321 BIC: $Bic \n" +
                "?30$Bic?31$Iban?32DANKITO\n" +
                ":62F:C200514EUR84,28\n" +
                "-"


        // when
        val result = underTest.parseMt940String(transactionsString)


        // then
        expect(result).hasSize(1)
        expect(result.flatMap { it.transactions }).hasSize(3)

        result.flatMap { it.transactions }.forEach { transaction ->
            expect(transaction.information).notToBeNull()

            expect(transaction.information?.sepaUsage).notToBeNull()

            if (transaction.information?.unparsedUsage?.contains("KREF+") == true) {
                expect(transaction.information?.customerReference).notToBeNull()
            }
        }
    }


    private fun assertBalance(balance: Balance, isCredit: Boolean, bookingDate: Date, amount: Amount) {
        expect(balance.isCredit).toBe(isCredit)
        expect(balance.bookingDate).toBe(bookingDate)
        expect(balance.amount).toBe(amount)
        expect(balance.currency).toBe(Currency)
    }

    private fun assertTurnover(statementLine: StatementLine, valueDate: Date, amount: Amount, isCredit: Boolean = true,
                               bookingDate: Date? = valueDate) {

        expect(statementLine.isCredit).toBe(isCredit)
        expect(statementLine.isReversal).isFalse()
        expect(statementLine.valueDate).toBe(valueDate)
        expect(statementLine.bookingDate).toBe(bookingDate)
        expect(statementLine.amount).toBe(amount)
    }

    private fun assertTransactionDetails(details: InformationToAccountOwner?, otherPartyName: String,
                                         otherPartyBankCode: String, otherPartyAccountId: String) {

        expect(details).notToBeNull()

        expect(details?.otherPartyName).toBe(otherPartyName)
        expect(details?.otherPartyBankCode).toBe(otherPartyBankCode)
        expect(details?.otherPartyAccountId).toBe(otherPartyAccountId)
    }


    private val AccountStatementWithSingleTransaction = """
        :20:STARTUMSE
        :25:$BankCode/$CustomerId
        :28C:00000/001
        :60F:C${convertMt940Date(AccountStatement1PreviousStatementBookingDate)}EUR$AccountStatement1OpeningBalanceAmount
        :61:${convertMt940Date(AccountStatement1BookingDate)}${convertToShortBookingDate(AccountStatement1BookingDate)}CR${AccountStatement1Transaction1Amount}N062NONREF
        :86:166?00GUTSCHR. UEBERWEISUNG?109249?20EREF+674?21SVWZ+1908301/
        EUR ${AccountStatement1Transaction1Amount}/20?2219-10-02/...?30$AccountStatement1Transaction1OtherPartyBankCode?31$AccountStatement1Transaction1OtherPartyAccountId
        ?32$AccountStatement1Transaction1OtherPartyName
        :62F:C${convertMt940Date(AccountStatement1BookingDate)}EUR$AccountStatement1ClosingBalanceAmount
        -
    """.trimIndent()

    private val AccountStatementWithTwoTransactions = """
        :20:STARTUMSE
        :25:$BankCode/$CustomerId
        :28C:00000/001
        :60F:C${convertMt940Date(AccountStatement1PreviousStatementBookingDate)}EUR$AccountStatement1OpeningBalanceAmount
        :61:${convertMt940Date(AccountStatement1BookingDate)}${convertToShortBookingDate(AccountStatement1BookingDate)}CR${AccountStatement1Transaction1Amount}N062NONREF
        :86:166?00GUTSCHR. UEBERWEISUNG?109249?20EREF+674?21SVWZ+1908301/
        EUR ${AccountStatement1Transaction1Amount}/20?2219-10-02/...?30$AccountStatement1Transaction1OtherPartyBankCode?31$AccountStatement1Transaction1OtherPartyAccountId
        ?32$AccountStatement1Transaction1OtherPartyName
        :61:${convertMt940Date(AccountStatement1BookingDate)}${convertToShortBookingDate(AccountStatement1BookingDate)}DR${AccountStatement1Transaction2Amount}N062NONREF
        :86:166?00ONLINE-UEBERWEISUNG?109249?20EREF+674?21SVWZ+1908301/
        EUR ${AccountStatement1Transaction2Amount}/20?2219-10-02/...?30$AccountStatement1Transaction2OtherPartyBankCode?31$AccountStatement1Transaction2OtherPartyAccountId
        ?32$AccountStatement1Transaction2OtherPartyName
        :62F:C${convertMt940Date(AccountStatement1BookingDate)}EUR${AccountStatement1With2TransactionsClosingBalanceAmount}
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
        return Mt940Parser.DateFormat.format(date.dateTimeDayStart.localUnadjusted)
    }

    private fun convertToShortBookingDate(date: Date): String {
        return DateFormat("MMdd").format(date.dateTimeDayStart.localUnadjusted)
    }

}
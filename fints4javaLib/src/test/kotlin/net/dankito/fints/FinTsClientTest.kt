package net.dankito.fints

import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatus
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatusWerte
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.fints.model.*
import net.dankito.fints.util.Java8Base64Service
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import java.util.*


@Ignore // not an automatic test, supply your settings below
class FinTsClientTest {

    private val underTest = FinTsClient(Java8Base64Service())


    private val BankDataAnonymous = BankData("10070000", Laenderkennzeichen.Germany, "https://fints.deutsche-bank.de/")

    // TODO: add your settings here:
    private val Bank = BankData("", Laenderkennzeichen.Germany, "", bic = "")
    private val Customer = CustomerData("", "", name = "", iban = "",
        selectedTanProcedure = TanProcedure("", Sicherheitsfunktion.PIN_TAN_911, TanProcedureType.ChipTan))

    // transfer 1 cent to yourself. Transferring money to oneself also doesn't require to enter a TAN according to PSD2
    private val BankTransferData = BankTransferData(Customer.name, Customer.iban!!, Bank.bic!!, 0.01.toBigDecimal(), "Give it to me baby")



    @Test
    fun getAnonymousBankInfo() {

        // when
        val result = underTest.getAnonymousBankInfo(BankDataAnonymous)

        // then
        assertThat(result.successful).isTrue()
    }


    @Test
    fun synchronizeCustomerSystemId() {

        // when
        val result = underTest.synchronizeCustomerSystemId(Bank, Customer)

        // then
        assertThat(result.successful).isTrue()
        assertThat(Customer.customerSystemId).isNotEqualTo(KundensystemStatus.SynchronizingCustomerSystemId) // customer system id is now set
        assertThat(Customer.selectedLanguage).isNotEqualTo(Dialogsprache.Default) // language is set now
        assertThat(Customer.customerSystemStatus).isEqualTo(KundensystemStatusWerte.Benoetigt) // customerSystemStatus is set now
    }


    @Test
    fun getTransactions() {

        // given
        // some banks support retrieving account transactions of last 90 days without TAN
        val ninetyDaysAgoMilliseconds = 90 * 24 * 60 * 60 * 1000L
        val ninetyDaysAgo = Date(Date().time - ninetyDaysAgoMilliseconds)

        // when
        val result = underTest.getTransactions(GetTransactionsParameter(fromDate = ninetyDaysAgo), Bank, Customer)

        // then
        assertThat(result.successful).isTrue()
    }


    @Test
    fun testBankTransfer() {

        // when
        val result = underTest.doBankTransfer(BankTransferData, Bank, Customer)

        // then
        assertThat(result.successful).isTrue()
    }

}
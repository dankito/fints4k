package net.dankito.fints

import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatus
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatusWerte
import net.dankito.fints.model.*
import net.dankito.fints.response.client.FinTsClientResponse
import net.dankito.fints.util.Java8Base64Service
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test


@Ignore // not an automatic test, supply your settings below
class FinTsClientTest {

    private val callback = object : FinTsClientCallback {

        override fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>): TanProcedure? {
            // TODO: if entering TAN is required select your tan procedure here
            return supportedTanProcedures.first()
        }

        override fun enterTan(tanChallenge: TanChallenge): String? {
            return null
        }

    }


    private val underTest = object : FinTsClient(callback, Java8Base64Service()) {

        fun testSynchronizeCustomerSystemId(bank: BankData, customer: CustomerData): FinTsClientResponse {
            return synchronizeCustomerSystemId(bank, customer)
        }

    }


    private val BankDataAnonymous = BankData("10070000", Laenderkennzeichen.Germany, "https://fints.deutsche-bank.de/")

    // TODO: add your settings here:
    private val Bank = BankData("", Laenderkennzeichen.Germany, "", bic = "")
    private val Customer = CustomerData("", "", iban = "")

    // transfer 1 cent to yourself. Transferring money to oneself also doesn't require to enter a TAN according to PSD2
    private val BankTransferData = BankTransferData(Customer.name, Customer.iban!!, Bank.bic!!, 0.01.toBigDecimal(), "Give it to me baby")



    @Test
    fun getAnonymousBankInfo() {

        // when
        val result = underTest.getAnonymousBankInfo(BankDataAnonymous)

        // then
        assertThat(result.isSuccessful).isTrue()
        assertThat(BankDataAnonymous.supportedHbciVersions).isNotEmpty()
        assertThat(BankDataAnonymous.supportedTanProcedures).isNotEmpty()
        assertThat(BankDataAnonymous.supportedJobs).isNotEmpty()
        assertThat(BankDataAnonymous.supportedLanguages).isNotEmpty()
        assertThat(BankDataAnonymous.name).isNotEmpty()
    }


    @Test
    fun synchronizeCustomerSystemId() {

        // when
        val result = underTest.testSynchronizeCustomerSystemId(Bank, Customer)

        // then
        assertThat(result.isSuccessful).isTrue()
        assertThat(Customer.customerSystemId).isNotEqualTo(KundensystemStatus.SynchronizingCustomerSystemId) // customer system id is now set
        assertThat(Customer.selectedLanguage).isNotEqualTo(Dialogsprache.Default) // language is set now
        assertThat(Customer.customerSystemStatus).isEqualTo(KundensystemStatusWerte.Benoetigt) // customerSystemStatus is set now
    }


    @Test
    fun getTransactions() {

        // when

        // some banks support retrieving account transactions of last 90 days without TAN
        val result = underTest.tryGetTransactionsOfLast90DaysWithoutTan(Bank, Customer)


        // then
        assertThat(result.isSuccessful).isTrue()
        assertThat(result.bookedTransactions).isNotEmpty()
    }


    @Test
    fun testBankTransfer() {

        // when
        val result = underTest.doBankTransfer(BankTransferData, Bank, Customer)

        // then
        assertThat(result.isSuccessful).isTrue()
    }

}
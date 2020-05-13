package net.dankito.fints

import net.dankito.fints.banks.InMemoryBankFinder
import net.dankito.fints.callback.FinTsClientCallback
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatus
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatusWerte
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanEinsatzOption
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedienArtVersion
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanMediumKlasse
import net.dankito.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.fints.model.*
import net.dankito.fints.model.mapper.BankDataMapper
import net.dankito.fints.response.client.FinTsClientResponse
import net.dankito.fints.util.Java8Base64Service
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


@Ignore // not an automatic test, supply your settings below
class FinTsClientTest {

    companion object {
        val DateTimeFormatForUniqueBankTransferUsage = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    }


    private val didAskUserForTanProcedure = AtomicBoolean(false)

    private val didAskUserToEnterTan = AtomicBoolean(false)


    private val callback = object : FinTsClientCallback {

        override fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>, suggestedTanProcedure: TanProcedure?): TanProcedure? {
            return suggestedTanProcedure // simply return suggestedTanProcedure as in most cases it's the best fitting one
        }

        override fun enterTan(customer: CustomerData, tanChallenge: TanChallenge): EnterTanResult {
            didAskUserToEnterTan.set(true)

            return EnterTanResult.userDidNotEnterTan()
        }

        override fun enterTanGeneratorAtc(customer: CustomerData, tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
            Assert.fail("Bank asks you to synchronize your TAN generator for card ${tanMedium.cardNumber} " +
                    "(card sequence number ${tanMedium.cardSequenceNumber}). Please do this via your online banking portal or Banking UI.")
            return EnterTanGeneratorAtcResult.userDidNotEnterAtc() // should actually never be called
        }

    }


    private val underTest = object : FinTsClient(callback, Java8Base64Service()) {

        fun testSynchronizeCustomerSystemId(bank: BankData, customer: CustomerData): FinTsClientResponse {
            return synchronizeCustomerSystemId(bank, customer)
        }

    }


    private val BankDataAnonymous = BankData("10070000", Laenderkennzeichen.Germany, "https://fints.deutsche-bank.de/", "DEUTDEBBXXX")

    // TODO: add your settings here:
    private val bankInfo = InMemoryBankFinder().findBankByBankCode("<your bank code (BLZ) here>").first()
    private val Bank = BankDataMapper().mapFromBankInfo(bankInfo)
    private val Customer = CustomerData("<your customer id (Kontonummer) here>", "<your PIN here>")



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
    fun addAccount() {

        // when
        val result = underTest.addAccount(Bank, Customer)

        // then
        assertThat(result.isSuccessful).isTrue()

        assertThat(didAskUserForTanProcedure).isFalse()

        assertThat(Bank.name).isNotEmpty()
        assertThat(Bank.supportedJobs).isNotEmpty() // supported jobs are now known
        assertThat(Bank.supportedTanProcedures).isNotEmpty() // supported tan procedures are now known
        assertThat(Bank.supportedHbciVersions).isNotEmpty() // supported HBIC versions are now known
        assertThat(Bank.supportedLanguages).isNotEmpty() // supported languages are now known

        assertThat(Customer.name).isNotEmpty()
        assertThat(Customer.supportedTanProcedures).isNotEmpty()
        assertThat(Customer.selectedLanguage).isNotEqualTo(Dialogsprache.Default) // language is set now
        assertThat(Customer.customerSystemId).isNotEqualTo(KundensystemStatus.SynchronizingCustomerSystemId) // customer system id is now set
        assertThat(Customer.customerSystemStatus).isEqualTo(KundensystemStatusWerte.Benoetigt) // customerSystemStatus is set now
        assertThat(Customer.accounts).isNotEmpty() // accounts are now known
        assertThat(Customer.accounts.first().allowedJobs).isNotEmpty() // allowed jobs are now known
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

        // given
        underTest.addAccount(Bank, Customer) // retrieve basic data, e.g. accounts
        val account = Customer.accounts.firstOrNull { it.supportsFeature(AccountFeature.RetrieveAccountTransactions) }
        assertThat(account).describedAs("We need at least one account that supports retrieving account transactions (${CustomerSegmentId.AccountTransactionsMt940.id})").isNotNull()

        // when

        // some banks support retrieving account transactions of last 90 days without TAN
        val result = underTest.tryGetTransactionsOfLast90DaysWithoutTan(Bank, Customer, account!!)


        // then
        assertThat(result.isSuccessful).isTrue()
        assertThat(result.bookedTransactions).isNotEmpty()
    }


    @Test
    fun getTanMediaList() {

        // this test is only senseful for accounts using chipTAN / TAN generator as TAN procedure

        underTest.getAnonymousBankInfo(Bank)

        val supportsRetrievingTanMedia = Bank.supportedJobs.firstOrNull { it.jobName == "HKTAB" } != null

        if (supportsRetrievingTanMedia == false) { // accounts with appTAN, pushTAN, smsTAN, ... would fail here -> simply return
            return
        }

        assertThat(Customer.tanMedia).isEmpty()


        // when
        val result = underTest.getTanMediaList(Bank, Customer, TanMedienArtVersion.Alle, TanMediumKlasse.AlleMedien)


        // then
        assertThat(result.isSuccessful).isTrue()

        assertThat(result.tanMediaList).isNotNull()
        assertThat(result.tanMediaList!!.usageOption).isEqualByComparingTo(TanEinsatzOption.KundeKannGenauEinMediumZuEinerZeitNutzen) // TODO: may adjust to your value
        assertThat(result.tanMediaList!!.tanMedia).isNotEmpty()

        assertThat(Customer.tanMedia).isNotEmpty()
    }

    @Ignore // only works with banks that don't support HKTAB version 5
    @Test(expected = UnsupportedOperationException::class)
    fun getTanMediaList_UnsupportedTanMediumClass() {

        // when
        underTest.getTanMediaList(Bank, Customer, TanMedienArtVersion.Alle, TanMediumKlasse.BilateralVereinbart)


        // then
        // exception gets thrown
    }


    @Test
    fun testBankTransfer() {

        // given
        underTest.addAccount(Bank, Customer) // retrieve basic data, e.g. accounts

        // we need at least one account that supports cash transfer
        val account = Customer.accounts.firstOrNull { it.supportsFeature(AccountFeature.TransferMoney) }
        assertThat(account).describedAs("We need at least one account that supports cash transfer (${CustomerSegmentId.SepaBankTransfer.id})").isNotNull()

        // IBAN should be set
        assertThat(account?.iban).describedAs("Account IBAN must be set").isNotNull()

        // transfer 1 cent to yourself. Transferring money to oneself also doesn't require to enter a TAN according to PSD2
        val BankTransferData = BankTransferData(Customer.name, account?.iban!!, Bank.bic, 0.01.toBigDecimal(),
            "${DateTimeFormatForUniqueBankTransferUsage.format(LocalDateTime.now())} Test transaction ${UUID.randomUUID()}")


        // when
        val result = underTest.doBankTransfer(BankTransferData, Bank, Customer, account)

        // then
        assertThat(result.isSuccessful).isTrue()
    }

}
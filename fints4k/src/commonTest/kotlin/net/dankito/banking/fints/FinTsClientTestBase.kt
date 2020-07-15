package net.dankito.banking.fints

import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.expect
import net.dankito.banking.bankfinder.InMemoryBankFinder
import net.dankito.banking.fints.callback.FinTsClientCallback
import net.dankito.banking.fints.extensions.isTrue
import net.dankito.banking.fints.extensions.isFalse
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.banking.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.banking.fints.messages.datenelemente.implementierte.KundensystemStatus
import net.dankito.banking.fints.messages.datenelemente.implementierte.KundensystemStatusWerte
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanEinsatzOption
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMedienArtVersion
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMediumKlasse
import net.dankito.banking.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.banking.fints.model.*
import net.dankito.banking.mapper.BankDataMapper
import net.dankito.banking.bankfinder.BankInfo
import net.dankito.banking.fints.response.client.FinTsClientResponse
import net.dankito.banking.fints.util.IBase64Service
import net.dankito.banking.fints.util.IThreadPool
import net.dankito.banking.fints.util.PureKotlinBase64Service
import net.dankito.banking.fints.webclient.KtorWebClient
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.DateFormatter
import net.dankito.utils.multiplatform.UUID
import kotlin.test.DefaultAsserter.fail
import kotlin.test.Ignore
import kotlin.test.Test


@Ignore // not an automatic test, supply your settings below
open class FinTsClientTestBase {

    companion object {
        val DateTimeFormatForUniqueBankTransferUsage = DateFormatter("yyyy-MM-dd'T'HH:mm:ss.SSS")
    }


    private var didAskUserForTanProcedure = false

    private var didAskUserToEnterTan = false


    private val callback = object : FinTsClientCallback {

        override fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>, suggestedTanProcedure: TanProcedure?): TanProcedure? {
            didAskUserForTanProcedure = true
            return suggestedTanProcedure // simply return suggestedTanProcedure as in most cases it's the best fitting one
        }

        override fun enterTan(customer: CustomerData, tanChallenge: TanChallenge): EnterTanResult {
            didAskUserToEnterTan = true

            return EnterTanResult.userDidNotEnterTan()
        }

        override fun enterTanGeneratorAtc(customer: CustomerData, tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
            fail("Bank asks you to synchronize your TAN generator for card ${tanMedium.cardNumber} " +
                    "(card sequence number ${tanMedium.cardSequenceNumber}). Please do this via your online banking portal or Banking UI.")
        }

    }


    private val underTest = object : FinTsClient(callback, KtorWebClient(), PureKotlinBase64Service()) {

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
        underTest.getAnonymousBankInfo(BankDataAnonymous) { result ->

            // then
            expect(result.isSuccessful).isTrue()
            expect(BankDataAnonymous.supportedHbciVersions).isNotEmpty()
            expect(BankDataAnonymous.supportedTanProcedures).isNotEmpty()
            expect(BankDataAnonymous.supportedJobs).isNotEmpty()
            expect(BankDataAnonymous.supportedLanguages).isNotEmpty()
            expect(BankDataAnonymous.name).isNotEmpty()
        }
    }


    @Test
    fun addAccount() {

        // when
        val result = underTest.addAccount(Bank, Customer)

        // then
        expect(result.isSuccessful).isTrue()

        expect(didAskUserForTanProcedure).isFalse()

        expect(Bank.name).isNotEmpty()
        expect(Bank.supportedJobs).isNotEmpty() // supported jobs are now known
        expect(Bank.supportedTanProcedures).isNotEmpty() // supported tan procedures are now known
        expect(Bank.supportedHbciVersions).isNotEmpty() // supported HBIC versions are now known
        expect(Bank.supportedLanguages).isNotEmpty() // supported languages are now known

        expect(Customer.name).isNotEmpty()
        expect(Customer.supportedTanProcedures).isNotEmpty()
        expect(Customer.selectedLanguage).notToBe(Dialogsprache.Default) // language is set now
        expect(Customer.customerSystemId).notToBe(KundensystemStatus.SynchronizingCustomerSystemId.code) // customer system id is now set
        expect(Customer.customerSystemStatus).toBe(KundensystemStatusWerte.Benoetigt) // customerSystemStatus is set now
        expect(Customer.accounts).isNotEmpty() // accounts are now known
        expect(Customer.accounts.first().allowedJobs).isNotEmpty() // allowed jobs are now known
    }


    @Test
    fun synchronizeCustomerSystemId() {

        // when
        val result = underTest.testSynchronizeCustomerSystemId(Bank, Customer)

        // then
        expect(result.isSuccessful).isTrue()
        expect(Customer.customerSystemId).notToBe(KundensystemStatus.SynchronizingCustomerSystemId.code) // customer system id is now set
        expect(Customer.selectedLanguage).notToBe(Dialogsprache.Default) // language is set now
        expect(Customer.customerSystemStatus).toBe(KundensystemStatusWerte.Benoetigt) // customerSystemStatus is set now
    }


    @ExperimentalWithOptions
    @Test
    fun getTransactions() {

        // given
        underTest.addAccount(Bank, Customer) // retrieve basic data, e.g. accounts
        val account = Customer.accounts.firstOrNull { it.supportsFeature(AccountFeature.RetrieveAccountTransactions) }
        expect(account).withRepresentation("We need at least one account that supports retrieving account transactions (${CustomerSegmentId.AccountTransactionsMt940.id})").notToBeNull()

        // when

        // some banks support retrieving account transactions of last 90 days without TAN
        val result = underTest.tryGetTransactionsOfLast90DaysWithoutTan(Bank, Customer, account!!)


        // then
        expect(result.isSuccessful).isTrue()
        expect(result.bookedTransactions).isNotEmpty()
    }


    @Test
    fun getTanMediaList() {

        // this test is only senseful for accounts using chipTAN / TAN generator as TAN procedure

        underTest.getAnonymousBankInfo(Bank) { }

        val supportsRetrievingTanMedia = Bank.supportedJobs.firstOrNull { it.jobName == "HKTAB" } != null

        if (supportsRetrievingTanMedia == false) { // accounts with appTAN, pushTAN, smsTAN, ... would fail here -> simply return
            return
        }

        expect(Customer.tanMedia).isEmpty()


        // when
        underTest.getTanMediaList(Bank, Customer, TanMedienArtVersion.Alle, TanMediumKlasse.AlleMedien) { result ->

            // then
            expect(result.isSuccessful).isTrue()

            expect(result.tanMediaList).notToBeNull()
            expect(result.tanMediaList!!.usageOption).toBe(TanEinsatzOption.KundeKannGenauEinMediumZuEinerZeitNutzen) // TODO: may adjust to your value
            expect(result.tanMediaList!!.tanMedia).isNotEmpty()

            expect(Customer.tanMedia).isNotEmpty()
        }
    }

    @Ignore // only works with banks that don't support HKTAB version 5
    @Test
    fun getTanMediaList_UnsupportedTanMediumClass() {

        // when
        expect {
            underTest.getTanMediaList(Bank, Customer, TanMedienArtVersion.Alle, TanMediumKlasse.BilateralVereinbart) { }
        }.toThrow<UnsupportedOperationException>()


        // then
        // exception gets thrown
    }


    @ExperimentalWithOptions
    @Test
    fun testBankTransfer() {

        // given
        underTest.addAccount(Bank, Customer) // retrieve basic data, e.g. accounts

        // we need at least one account that supports cash transfer
        val account = Customer.accounts.firstOrNull { it.supportsFeature(AccountFeature.TransferMoney) }
        expect(account).withRepresentation("We need at least one account that supports cash transfer (${CustomerSegmentId.SepaBankTransfer.id})").notToBeNull()

        // IBAN should be set
        expect(account?.iban).withRepresentation("Account IBAN must be set").notToBeNull()

        // transfer 1 cent to yourself. Transferring money to oneself also doesn't require to enter a TAN according to PSD2
        val BankTransferData = BankTransferData(Customer.name, account?.iban!!, Bank.bic, Money(Amount("0,01"), "EUR"),
            "${DateTimeFormatForUniqueBankTransferUsage.format(Date())} Test transaction ${UUID.random()}")


        // when
        underTest.doBankTransfer(BankTransferData, Bank, Customer, account) { result ->

            // then
            expect(result.isSuccessful).isTrue()
        }
    }

}
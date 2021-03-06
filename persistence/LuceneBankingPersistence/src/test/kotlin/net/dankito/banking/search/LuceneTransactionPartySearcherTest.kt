package net.dankito.banking.search

import net.dankito.banking.persistence.LuceneBankingPersistence
import net.dankito.banking.ui.model.BankData
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import net.dankito.utils.io.FileUtils
import net.dankito.utils.multiplatform.File
import net.dankito.utils.multiplatform.toBigDecimal
import net.dankito.utils.multiplatform.toDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ThreadLocalRandom


class LuceneTransactionPartySearcherTest {

    companion object {

        private val dataFolder = File("testData")

        private val databaseFolder = File(dataFolder, "db")

        private val indexFolder = File(dataFolder, "index")


        private val BookingDate = "27.03.2020"
        private val OtherPartyName = "Mahatma Gandhi"
        private val OtherPartyBankCode = "12345678"
        private val OtherPartyAccountId = "0987654321"
        private val Amount = BigDecimal.valueOf(123.45)


        private val bankAccountMock = BankAccount(mock(BankData::class.java), "", "", null, null, "")


        private val dateFormat = SimpleDateFormat("dd.MM.yyyy")

    }


    private val fileUtils = FileUtils()

    private val bankingPersistence = LuceneBankingPersistence(indexFolder, databaseFolder)

    private val underTest = LuceneTransactionPartySearcher(indexFolder)


    @Before
    fun setUp() {
        clearDataFolder()
    }

    @After
    fun tearDown() {
        clearDataFolder()
    }

    private fun clearDataFolder() {
        fileUtils.deleteFolderRecursively(dataFolder)
    }


    @Test
    fun findTransactionParty_ByFullName() {

        // given
        val query = OtherPartyName

        val before = underTest.findTransactionParty(query)
        assertThat(before).isEmpty()

        bankingPersistence.saveOrUpdateAccountTransactions(bankAccountMock, listOf(
            createTransaction(bankAccountMock, BookingDate, Amount, OtherPartyName, OtherPartyBankCode, OtherPartyAccountId),
            createTransaction(),
            createTransaction()
        ))


        // when
        val result = underTest.findTransactionParty(query)


        // then
        assertThat(result).hasSize(1)
        assertThat(result.first().name).isEqualTo(OtherPartyName)
        assertThat(result.first().bic).isEqualTo(OtherPartyBankCode)
        assertThat(result.first().iban).isEqualTo(OtherPartyAccountId)
    }

    @Test
    fun findTransactionParty_ByPartialName() {

        // given
        val query = "gand"

        val before = underTest.findTransactionParty(query)
        assertThat(before).isEmpty()

        bankingPersistence.saveOrUpdateAccountTransactions(bankAccountMock, listOf(
            createTransaction(bankAccountMock, BookingDate, Amount, OtherPartyName, OtherPartyBankCode, OtherPartyAccountId),
            createTransaction(),
            createTransaction()
        ))


        // when
        val result = underTest.findTransactionParty(query)


        // then
        assertThat(result).hasSize(1)
        assertThat(result.first().name).isEqualTo(OtherPartyName)
        assertThat(result.first().bic).isEqualTo(OtherPartyBankCode)
        assertThat(result.first().iban).isEqualTo(OtherPartyAccountId)
    }

    @Test
    fun findTransactionParty_SimilarNames() {

        // given
        val query = "gand"
        val secondOtherPartyName = "Gandalf"

        val before = underTest.findTransactionParty(query)
        assertThat(before).isEmpty()

        bankingPersistence.saveOrUpdateAccountTransactions(bankAccountMock, listOf(
            createTransaction(bankAccountMock, BookingDate, Amount, OtherPartyName, OtherPartyBankCode, OtherPartyAccountId),
            createTransaction(otherPartyName = secondOtherPartyName),
            createTransaction()
        ))


        // when
        val result = underTest.findTransactionParty(query)


        // then
        assertThat(result).hasSize(2)
        assertThat(result.map { it.name }).containsExactlyInAnyOrder(OtherPartyName, secondOtherPartyName)
    }

    @Test
    fun findTransactionParty_DuplicateEntries() {

        // given
        val query = OtherPartyName

        val before = underTest.findTransactionParty(query)
        assertThat(before).isEmpty()

        bankingPersistence.saveOrUpdateAccountTransactions(bankAccountMock, listOf(
            createTransaction(bankAccountMock, BookingDate, Amount, OtherPartyName, OtherPartyBankCode, OtherPartyAccountId),
            createTransaction(bankAccountMock, "01.02.2020", Amount, OtherPartyName, OtherPartyBankCode, OtherPartyAccountId),
            createTransaction(bankAccountMock, "03.04.2020", Amount, OtherPartyName, OtherPartyBankCode, OtherPartyAccountId),
            createTransaction(),
            createTransaction()
        ))


        // when
        val result = underTest.findTransactionParty(query)


        // then
        assertThat(result).hasSize(1)
        assertThat(result.first().name).isEqualTo(OtherPartyName)
        assertThat(result.first().bic).isEqualTo(OtherPartyBankCode)
        assertThat(result.first().iban).isEqualTo(OtherPartyAccountId)
    }

    @Test
    fun findTransactionParty_OtherName() {

        // given
        val query = "Mandela"

        val before = underTest.findTransactionParty(query)
        assertThat(before).isEmpty()

        bankingPersistence.saveOrUpdateAccountTransactions(bankAccountMock, listOf(
            createTransaction(bankAccountMock, BookingDate, Amount, OtherPartyName, OtherPartyBankCode, OtherPartyAccountId),
            createTransaction(),
            createTransaction()
        ))


        // when
        val result = underTest.findTransactionParty(query)


        // then
        assertThat(result).isEmpty()
    }


    private fun createTransaction(bankAccount: BankAccount = bankAccountMock, bookingDate: String, amount: BigDecimal = randomBigDecimal(),
                                  otherPartyName: String = randomString(), otherPartyBankCode: String = randomString(),
                                  otherPartyAccountId: String = randomString(), reference: String = randomString()): AccountTransaction {

        return createTransaction(bankAccount, dateFormat.parse(bookingDate), amount, otherPartyName,
            otherPartyBankCode, otherPartyAccountId, reference)
    }

    private fun createTransaction(bankAccount: BankAccount = bankAccountMock, bookingDate: Date = randomDate(), amount: BigDecimal = randomBigDecimal(),
                                  otherPartyName: String = randomString(), otherPartyBankCode: String = randomString(),
                                  otherPartyAccountId: String = randomString(), reference: String = randomString()): AccountTransaction {

        return AccountTransaction(bankAccount, amount.toBigDecimal(), "EUR", reference, bookingDate.toDate(), otherPartyName, otherPartyBankCode, otherPartyAccountId, null, bookingDate.toDate())
    }

    private fun randomString(): String {
        return UUID.randomUUID().toString()
    }

    private fun randomDate(): Date {
        val pseudoRandomLong = ThreadLocalRandom.current().nextLong(0, Date().time)

        return Date(pseudoRandomLong)
    }

    private fun randomBigDecimal(): BigDecimal {
        val pseudoRandomDouble = ThreadLocalRandom.current().nextDouble(-5-000.0, 12_000.0)

        return BigDecimal.valueOf(pseudoRandomDouble)
    }

}
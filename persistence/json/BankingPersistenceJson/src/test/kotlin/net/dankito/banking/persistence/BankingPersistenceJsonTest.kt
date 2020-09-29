package net.dankito.banking.persistence

import net.dankito.banking.persistence.model.AccountTransactionEntity
import net.dankito.banking.persistence.model.BankAccountEntity
import net.dankito.banking.persistence.model.BankDataEntity
import net.dankito.banking.ui.model.*
import net.dankito.banking.util.JacksonJsonSerializer
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.File
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.Test
import kotlin.random.Random


class BankingPersistenceJsonTest {

    companion object {

        const val BankCode = "12345678"

        const val CustomerId = "0987654321"

        const val Password = "12345"

        const val FinTsServerAddress = "http://i-do-not-exist.fail/givemeyourmoney"

        const val BankName = "Abzock GmbH"

        const val Bic = "ABCDDEBB123"

        const val CustomerName = "Hans Dampf"

        const val UserId = CustomerId

        const val IconUrl = "http://i-do-not-exist.fail/favicon.ico"

        val NowMillis = System.currentTimeMillis()

        val TwoYearsAgoMillis = NowMillis - (2 * 365 * 24 * 60 * 60 * 1000L)


        val TestDataFolder = File("testData")

        init {
            TestDataFolder.mkdirs()
        }
    }


    private val file = File(TestDataFolder, BankingPersistenceJson.BanksJsonFileName)

    private val serializer = JacksonJsonSerializer()

    private val underTest = BankingPersistenceJson(TestDataFolder, serializer)


    @Test
    fun saveOrUpdateBank() {

        // given
        val banks = listOf(
            createBank(2),
            createBank(3)
        )


        // when
        underTest.saveOrUpdateBank(banks.first() as TypedBankData, banks.map { it as TypedBankData })


        // then
        val result = serializer.deserializeListOr(file, BankDataEntity::class)

        assertBanksEqual(result, banks)
    }

    @Test
    fun saveOrUpdateBankWithAccountsAndTransactions() {

        // given
        val bank = createBank(2)


        // when
        underTest.saveOrUpdateBank(bank as TypedBankData, listOf(bank).map { it as TypedBankData })


        // then
        val result = serializer.deserializeListOr(file, BankDataEntity::class)

        assertBanksEqual(result, listOf(bank) as List<BankDataEntity>)
    }


    @Test
    fun readPersistedBanks() {

        // given
        val banks = listOf(
            createBank(2),
            createBank(3)
        )

        serializer.serializeObject(banks, file)


        // when
        val result = underTest.readPersistedBanks()


        // then
        assertBanksEqual(banks, result as List<BankDataEntity>)
    }


    private fun createBank(countAccounts: Int = 0, customerId: String = CustomerId): BankDataEntity {
        val result = BankDataEntity(BankCode, customerId, Password, FinTsServerAddress, BankName, Bic, CustomerName, UserId, IconUrl)

        result.accounts = createAccounts(countAccounts, result)

        return result
    }

    private fun createAccounts(count: Int, customer: BankDataEntity): List<BankAccountEntity> {
        val random = Random(System.nanoTime())

        return IntRange(1, count).map { accountIndex ->
            createAccount("Account_$accountIndex", customer, random.nextInt(2, 50))
        }
    }

    private fun createAccount(productName: String, customer: BankDataEntity, countTransactions: Int = 0): BankAccountEntity {
        val result = BankAccountEntity(customer, customer.userName, "AccountHolder", "DE00" + customer.bankCode + customer.userName, null,
        BigDecimal(84.25), productName = productName)

        result.bookedTransactions = createTransactions(countTransactions, result)

        return result
    }

    private fun createTransactions(countTransactions: Int, account: BankAccountEntity): List<AccountTransactionEntity> {
        return IntRange(1, countTransactions).map { transactionIndex ->
            createTransaction(transactionIndex, account)
        }
    }

    private fun createTransaction(transactionIndex: Int, account: BankAccountEntity): AccountTransactionEntity {
        return AccountTransactionEntity(account, "OtherParty_$transactionIndex", "Reference_$transactionIndex", BigDecimal(transactionIndex.toDouble()), createDate(), null)
    }

    private fun createDate(): Date {
        return Date(Random(System.nanoTime()).nextLong(TwoYearsAgoMillis, NowMillis))
    }


    private fun assertBanksEqual(deserializedBanks: List<BankDataEntity>, banks: List<BankDataEntity>) {
        assertThat(deserializedBanks.size).isEqualTo(banks.size)

        deserializedBanks.forEach { deserializedBanks ->
            val bank = banks.firstOrNull { it.technicalId == deserializedBanks.technicalId }

            if (bank == null) {
                Assert.fail("Could not find matching bank for deserialized bank $deserializedBanks. banks = $banks")
            }
            else {
                assertBanksEqual(deserializedBanks, bank)
            }
        }
    }

    private fun assertBanksEqual(deserializedBank: BankDataEntity, bank: BankDataEntity) {
        assertThat(deserializedBank.bankCode).isEqualTo(bank.bankCode)
        assertThat(deserializedBank.userName).isEqualTo(bank.userName)
        assertThat(deserializedBank.password).isEqualTo(bank.password)
        assertThat(deserializedBank.finTsServerAddress).isEqualTo(bank.finTsServerAddress)

        assertThat(deserializedBank.bankName).isEqualTo(bank.bankName)
        assertThat(deserializedBank.bic).isEqualTo(bank.bic)
        assertThat(deserializedBank.customerName).isEqualTo(bank.customerName)
        assertThat(deserializedBank.userId).isEqualTo(bank.userId)
        assertThat(deserializedBank.iconUrl).isEqualTo(bank.iconUrl)

        assertAccountsEqual(deserializedBank.accounts, bank.accounts)
    }

    private fun assertAccountsEqual(deserializedAccounts: List<BankAccountEntity>, accounts: List<BankAccountEntity>) {
        assertThat(deserializedAccounts.size).isEqualTo(accounts.size)

        deserializedAccounts.forEach { deserializedAccount ->
            val account = accounts.firstOrNull { it.technicalId == deserializedAccount.technicalId }

            if (account == null) {
                Assert.fail("Could not find matching account for deserialized account $deserializedAccount. accounts = $accounts")
            }
            else {
                assertAccountsEqual(deserializedAccount, account)
            }
        }
    }

    private fun assertAccountsEqual(deserializedAccount: BankAccountEntity, account: BankAccountEntity) {
        // to check if MapStruct created reference correctly
        assertThat(deserializedAccount.bank.technicalId).isEqualTo(account.bank.technicalId)

        assertThat(deserializedAccount.identifier).isEqualTo(account.identifier)
        assertThat(deserializedAccount.iban).isEqualTo(account.iban)
        assertThat(deserializedAccount.balance).isEqualTo(account.balance)
        assertThat(deserializedAccount.productName).isEqualTo(account.productName)

        assertTransactionsEqual(deserializedAccount.bookedTransactions, account.bookedTransactions)
    }

    private fun assertTransactionsEqual(deserializedTransactions: List<AccountTransactionEntity>, transactions: List<AccountTransactionEntity>) {
        assertThat(deserializedTransactions.size).isEqualTo(transactions.size)

        deserializedTransactions.forEach { deserializedTransaction ->
            val transaction = transactions.firstOrNull { it.technicalId == deserializedTransaction.technicalId }

            if (transaction == null) {
                Assert.fail("Could not find matching transaction for deserialized transaction $deserializedTransaction. transactions = $transactions")
            }
            else {
                assertTransactionsEqual(deserializedTransaction, transaction)
            }
        }
    }

    private fun assertTransactionsEqual(deserializedTransaction: AccountTransactionEntity, transaction: AccountTransactionEntity) {
        // to check if MapStruct created reference correctly
        assertThat(deserializedTransaction.account.technicalId).isEqualTo(transaction.account.technicalId)

        assertThat(deserializedTransaction.otherPartyName).isEqualTo(transaction.otherPartyName)
        assertThat(deserializedTransaction.unparsedReference).isEqualTo(transaction.unparsedReference)
        assertThat(deserializedTransaction.amount).isEqualTo(transaction.amount)
        assertThat(deserializedTransaction.valueDate).isEqualTo(transaction.valueDate)
    }

}
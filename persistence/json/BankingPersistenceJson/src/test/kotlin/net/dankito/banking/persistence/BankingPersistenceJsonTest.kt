package net.dankito.banking.persistence

import net.dankito.banking.persistence.model.AccountTransactionEntity
import net.dankito.banking.persistence.model.BankAccountEntity
import net.dankito.banking.persistence.model.CustomerEntity
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


    private val file = File(TestDataFolder, "test_accounts.json")

    private val serializer = JacksonJsonSerializer()

    private val underTest = BankingPersistenceJson(file, serializer)


    @Test
    fun saveOrUpdateAccount() {

        // given
        val customers = listOf(
            createCustomer(2),
            createCustomer(3)
        )


        // when
        underTest.saveOrUpdateAccount(customers.first() as TypedCustomer, customers.map { it as TypedCustomer })


        // then
        val result = serializer.deserializeListOr(file, CustomerEntity::class)

        assertCustomersEqual(result, customers)
    }

    @Test
    fun saveOrUpdateAccountWithBankAccountsAndTransactions() {

        // given
        val customer = createCustomer(2)


        // when
        underTest.saveOrUpdateAccount(customer as TypedCustomer, listOf(customer).map { it as TypedCustomer })


        // then
        val result = serializer.deserializeListOr(file, CustomerEntity::class)

        assertCustomersEqual(result, listOf(customer) as List<CustomerEntity>)
    }


    @Test
    fun readPersistedAccounts() {

        // given
        val customers = listOf(
            createCustomer(2),
            createCustomer(3)
        )

        serializer.serializeObject(customers, file)


        // when
        val result = underTest.readPersistedAccounts()


        // then
        assertCustomersEqual(customers, result as List<CustomerEntity>)
    }


    private fun createCustomer(countBankAccounts: Int = 0, customerId: String = CustomerId): CustomerEntity {
        val result = CustomerEntity(BankCode, customerId, Password, FinTsServerAddress, BankName, Bic, CustomerName, UserId, IconUrl)

        result.accounts = createBankAccounts(countBankAccounts, result)

        return result
    }

    private fun createBankAccounts(count: Int, customer: CustomerEntity): List<BankAccountEntity> {
        val random = Random(System.nanoTime())

        return IntRange(1, count).map { accountIndex ->
            createBankAccount("Account_$accountIndex", customer, random.nextInt(2, 50))
        }
    }

    private fun createBankAccount(productName: String, customer: CustomerEntity, countTransactions: Int = 0): BankAccountEntity {
        val result = BankAccountEntity(customer, customer.customerId, "AccountHolder", "DE00" + customer.bankCode + customer.customerId, null,
        customer.customerId, BigDecimal(84.25), productName = productName)

        result.bookedTransactions = createAccountTransactions(countTransactions, result)

        return result
    }

    private fun createAccountTransactions(countTransactions: Int, account: BankAccountEntity): List<AccountTransactionEntity> {
        return IntRange(1, countTransactions).map { transactionIndex ->
            createAccountTransaction(transactionIndex, account)
        }
    }

    private fun createAccountTransaction(transactionIndex: Int, account: BankAccountEntity): AccountTransactionEntity {
        return AccountTransactionEntity(account, "OtherParty_$transactionIndex", "Usage_$transactionIndex", BigDecimal(transactionIndex.toDouble()), createDate(), null)
    }

    private fun createDate(): Date {
        return Date(Random(System.nanoTime()).nextLong(TwoYearsAgoMillis, NowMillis))
    }


    private fun assertCustomersEqual(deserializedCustomers: List<CustomerEntity>, customers: List<CustomerEntity>) {
        assertThat(deserializedCustomers.size).isEqualTo(customers.size)

        deserializedCustomers.forEach { deserializedCustomer ->
            val customer = customers.firstOrNull { it.technicalId == deserializedCustomer.technicalId }

            if (customer == null) {
                Assert.fail("Could not find matching customer for deserialized customer $deserializedCustomer. customers = $customers")
            }
            else {
                assertCustomersEqual(deserializedCustomer, customer)
            }
        }
    }

    private fun assertCustomersEqual(deserializedCustomer: CustomerEntity, customer: CustomerEntity) {
        assertThat(deserializedCustomer.bankCode).isEqualTo(customer.bankCode)
        assertThat(deserializedCustomer.customerId).isEqualTo(customer.customerId)
        assertThat(deserializedCustomer.password).isEqualTo(customer.password)
        assertThat(deserializedCustomer.finTsServerAddress).isEqualTo(customer.finTsServerAddress)

        assertThat(deserializedCustomer.bankName).isEqualTo(customer.bankName)
        assertThat(deserializedCustomer.bic).isEqualTo(customer.bic)
        assertThat(deserializedCustomer.customerName).isEqualTo(customer.customerName)
        assertThat(deserializedCustomer.userId).isEqualTo(customer.userId)
        assertThat(deserializedCustomer.iconUrl).isEqualTo(customer.iconUrl)

        assertBankAccountsEqual(deserializedCustomer.accounts, customer.accounts)
    }

    private fun assertBankAccountsEqual(deserializedAccounts: List<BankAccountEntity>, accounts: List<BankAccountEntity>) {
        assertThat(deserializedAccounts.size).isEqualTo(accounts.size)

        deserializedAccounts.forEach { deserializedAccount ->
            val account = accounts.firstOrNull { it.technicalId == deserializedAccount.technicalId }

            if (account == null) {
                Assert.fail("Could not find matching account for deserialized account $deserializedAccount. accounts = $accounts")
            }
            else {
                assertBankAccountsEqual(deserializedAccount, account)
            }
        }
    }

    private fun assertBankAccountsEqual(deserializedAccount: BankAccountEntity, account: BankAccountEntity) {
        // to check if MapStruct created reference correctly
        assertThat(deserializedAccount.customer.technicalId).isEqualTo(account.customer.technicalId)

        assertThat(deserializedAccount.identifier).isEqualTo(account.identifier)
        assertThat(deserializedAccount.iban).isEqualTo(account.iban)
        assertThat(deserializedAccount.customerId).isEqualTo(account.customerId)
        assertThat(deserializedAccount.balance).isEqualTo(account.balance)
        assertThat(deserializedAccount.productName).isEqualTo(account.productName)

        assertAccountTransactionsEqual(deserializedAccount.bookedTransactions, account.bookedTransactions)
    }

    private fun assertAccountTransactionsEqual(deserializedTransactions: List<AccountTransactionEntity>, transactions: List<AccountTransactionEntity>) {
        assertThat(deserializedTransactions.size).isEqualTo(transactions.size)

        deserializedTransactions.forEach { deserializedTransaction ->
            val transaction = transactions.firstOrNull { it.technicalId == deserializedTransaction.technicalId }

            if (transaction == null) {
                Assert.fail("Could not find matching transaction for deserialized transaction $deserializedTransaction. transactions = $transactions")
            }
            else {
                assertAccountTransactionsEqual(deserializedTransaction, transaction)
            }
        }
    }

    private fun assertAccountTransactionsEqual(deserializedTransaction: AccountTransactionEntity, transaction: AccountTransactionEntity) {
        // to check if MapStruct created reference correctly
        assertThat(deserializedTransaction.bankAccount.technicalId).isEqualTo(transaction.bankAccount.technicalId)

        assertThat(deserializedTransaction.otherPartyName).isEqualTo(transaction.otherPartyName)
        assertThat(deserializedTransaction.unparsedUsage).isEqualTo(transaction.unparsedUsage)
        assertThat(deserializedTransaction.amount).isEqualTo(transaction.amount)
        assertThat(deserializedTransaction.valueDate).isEqualTo(transaction.valueDate)
    }

}
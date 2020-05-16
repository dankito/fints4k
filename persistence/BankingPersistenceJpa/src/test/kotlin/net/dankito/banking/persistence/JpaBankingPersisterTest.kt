package net.dankito.banking.persistence

import net.dankito.banking.persistence.dao.AccountDao
import net.dankito.banking.persistence.entities.AccountEntity
import net.dankito.banking.persistence.entities.BankAccountEntity
import net.dankito.banking.persistence.entities.BankEntity
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.Bank
import net.dankito.banking.ui.model.BankAccount
import net.dankito.jpa.entitymanager.EntityManagerConfiguration
import net.dankito.utils.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File
import java.math.BigDecimal


internal class JpaBankingPersisterTest {

    companion object {
        private val TestBank = Bank("GLS Gemeinschaftsbank", "43060967", "GENODEM1GLS", "https://hbci-pintan.gad.de/cgi-bin/hbciservlet")

        private val TestAccount = Account(TestBank, "9876543210", "12345", "Monika Tester")

        private val TestBankAccount1 = BankAccount(TestAccount, TestAccount.customerId, "Sichteinlagen", "DE77${TestBank.bankCode}${TestAccount.customerId}", "01", BigDecimal.valueOf(77))
        private val TestBankAccount2 = BankAccount(TestAccount, TestAccount.customerId, "Festgeldkonto", "DE77${TestBank.bankCode}${TestAccount.customerId}", "02", BigDecimal.valueOf(88))
    }


    private val fileUtils = FileUtils()

    private val dataFolder = File("test_data")

    private val entityManager = JavaCouchbaseLiteEntityManager(EntityManagerConfiguration(dataFolder.path, "test"))

    private val accountDao = AccountDao(entityManager)

    private val underTest = JpaBankingPersister(accountDao)


    @AfterEach
    fun cleanCreatedDataAfterTest() {
        clearDataFolder()
    }


    private fun clearDataFolder() {
        fileUtils.deleteFolderRecursively(dataFolder)
    }


    @Test
    fun `Save account without BankAccounts`() {

        // given
        assertThat(accountDao.getAll()).isEmpty()


        // when
        underTest.saveOrUpdateAccount(TestAccount)


        // then
        val result = accountDao.getAll()

        assertThat(result).hasSize(1)

        assertAccount(result)
        assertBank(result.map { it.bank })
    }

    @Test
    fun `Save account without AccountTransactions`() {

        // given
        assertThat(accountDao.getAll()).isEmpty()

        TestAccount.bankAccounts = listOf(TestBankAccount1, TestBankAccount2)


        // when
        underTest.saveOrUpdateAccount(TestAccount)


        // then
        val result = accountDao.getAll()

        assertThat(result).hasSize(1)

        assertAccount(result)
        assertBank(result.map { it.bank })

        val bankAccounts = result.flatMap { it.bankAccounts }
        assertThat(bankAccounts).hasSize(2)
        assertThat(bankAccounts.map { it.account }.toSet()).containsExactly(result.get(0))

        assertBankAccount(bankAccounts.get(0), TestBankAccount1)
        assertBankAccount(bankAccounts.get(1), TestBankAccount2)
    }


    private fun assertAccount(result: List<AccountEntity>) {
        assertThat(result.map { it.customerId }).containsExactly(TestAccount.customerId)
        assertThat(result.map { it.password }).containsExactly(TestAccount.password)
        assertThat(result.map { it.name }).containsExactly(TestAccount.name)
    }

    private fun assertBank(result: List<BankEntity>) {
        assertThat(result.map { it.name }).containsExactly(TestBank.name)
        assertThat(result.map { it.bankCode }).containsExactly(TestBank.bankCode)
        assertThat(result.map { it.bic }).containsExactly(TestBank.bic)
        assertThat(result.map { it.finTsServerAddress }).containsExactly(TestBank.finTsServerAddress)
    }

    private fun assertBankAccount(bankAccount: BankAccountEntity, expectedBankAccount: BankAccount) {
        assertThat(bankAccount.identifier).isEqualTo(expectedBankAccount.identifier)
        assertThat(bankAccount.name).isEqualTo(expectedBankAccount.accountHolderName)
        assertThat(bankAccount.iban).isEqualTo(expectedBankAccount.iban)
        assertThat(bankAccount.subAccountNumber).isEqualTo(expectedBankAccount.subAccountNumber)
        assertThat(bankAccount.balance).isEqualTo(expectedBankAccount.balance)
        assertThat(bankAccount.currency).isEqualTo(expectedBankAccount.currency)
        assertThat(bankAccount.type).isEqualTo(expectedBankAccount.type)
    }

}
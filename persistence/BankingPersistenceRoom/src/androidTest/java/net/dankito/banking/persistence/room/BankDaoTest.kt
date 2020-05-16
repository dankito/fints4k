package net.dankito.banking.persistence.room

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.dankito.banking.persistence.room.entities.BankEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class BankDaoTest {

    companion object {
        private val TestBank = BankEntity(0, "10070000", "https://fints.deutsche-bank.de/", "DEUTDEBBXXX", "Deutsche Bank Fil Berlin")
    }

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: BankingDatabase


    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears after test
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BankingDatabase::class.java)
            // allowing main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        database.close()
    }


    @Test
    fun getUsersWhenNoUserInserted() {

        // when
        val result = database.bankDao().getAll()

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun insertAndGetUser() {

        // given
        database.bankDao().insert(TestBank)

        // when
        val result = database.bankDao().getAll()

        // then
        assertThat(result).hasSize(1)
        assertThat(result).extracting("bankCode").containsExactly(TestBank.bankCode)
        assertThat(result).extracting("finTsServerAddress").containsExactly(TestBank.finTsServerAddress)
        assertThat(result).extracting("bic").containsExactly(TestBank.bic)
        assertThat(result).extracting("name").containsExactly(TestBank.name)
    }

}

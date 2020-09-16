package net.dankito.banking.persistence

import android.content.Context
import androidx.room.Room
import net.dankito.banking.persistence.dao.saveOrUpdate
import net.dankito.banking.persistence.model.*
import net.dankito.banking.search.IRemitteeSearcher
import net.dankito.banking.search.Remittee
import net.dankito.banking.ui.model.IAccountTransaction
import net.dankito.banking.ui.model.TypedBankAccount
import net.dankito.banking.ui.model.TypedCustomer
import net.dankito.banking.ui.model.tan.MobilePhoneTanMedium
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium
import net.dankito.banking.util.persistence.doSaveUrlToFile
import net.dankito.utils.multiplatform.File


open class RoomBankingPersistence(applicationContext: Context) : IBankingPersistence, IRemitteeSearcher {

    protected val db = Room.databaseBuilder(
        applicationContext,
        BankingDatabase::class.java, "banking-database"
    ).build()


    override fun saveOrUpdateAccount(customer: TypedCustomer, allCustomers: List<TypedCustomer>) {
        (customer as? Bank)?.let { bank ->
            bank.selectedTanProcedureId = bank.selectedTanProcedure?.technicalId

            db.bankDao().saveOrUpdate(bank)

            // TODO: in this way removed accounts won't be deleted from DB and therefore still be visible to user
            val accounts = bank.accounts.filterIsInstance<BankAccount>()
            accounts.forEach { it.bankId = bank.id }
            db.bankAccountDao().saveOrUpdate(accounts)

            // TODO: in this way removed TAN procedures won't be deleted from DB and therefore still be visible to user
            val tanProcedures = bank.supportedTanProcedures.filterIsInstance<TanProcedure>()
            tanProcedures.forEach { it.bankId = bank.id }
            db.tanProcedureDao().saveOrUpdate(tanProcedures)

            // TODO: in this way removed TAN procedures won't be deleted from DB and therefore still be visible to user
            val tanMedia = bank.tanMedia.map { map(bank, it) }
            db.tanMediumDao().saveOrUpdate(tanMedia)
        }
    }

    override fun deleteAccount(customer: TypedCustomer, allCustomers: List<TypedCustomer>) {
        (customer as? Bank)?.let { bank ->
            db.accountTransactionDao().delete(bank.accounts.flatMap { it.bookedTransactions }.filterIsInstance<AccountTransaction>())

            db.bankAccountDao().delete(bank.accounts.filterIsInstance<BankAccount>())

            db.tanProcedureDao().delete(bank.supportedTanProcedures.filterIsInstance<TanProcedure>())
            db.tanMediumDao().delete(bank.tanMedia.filterIsInstance<TanMedium>())

            db.bankDao().delete(bank)
        }
    }

    override fun readPersistedAccounts(): List<TypedCustomer> {
        val banks = db.bankDao().getAll()

        val accounts = db.bankAccountDao().getAll()

        val transactions = db.accountTransactionDao().getAll()

        val tanProcedures = db.tanProcedureDao().getAll()

        val tanMedia = db.tanMediumDao().getAll()

        banks.forEach { bank ->
            bank.accounts = accounts.filter { it.bankId == bank.id }

            bank.accounts.filterIsInstance<BankAccount>().forEach { account ->
                account.customer = bank

                account.bookedTransactions = transactions.filter { it.bankAccountId == account.id }

                account.bookedTransactions.filterIsInstance<AccountTransaction>().forEach { transaction ->
                    transaction.bankAccount = account
                }
            }

            bank.supportedTanProcedures = tanProcedures.filter { it.bankId == bank.id }
            bank.selectedTanProcedure = bank.supportedTanProcedures.firstOrNull { it.technicalId == bank.selectedTanProcedureId }

            bank.tanMedia = tanMedia.filter { it.bankId == bank.id }.map { map(it) }
        }

        return banks
    }

    override fun saveOrUpdateAccountTransactions(bankAccount: TypedBankAccount, transactions: List<IAccountTransaction>) {
        val accountId = (bankAccount as? BankAccount)?.id ?: bankAccount.technicalId.toLong()

        val mappedTransactions = transactions.filterIsInstance<AccountTransaction>()

        mappedTransactions.forEach { it.bankAccountId = accountId }

        db.accountTransactionDao().saveOrUpdate(mappedTransactions)
    }


    protected open fun map(bank: Bank, tanMedium: net.dankito.banking.ui.model.tan.TanMedium): TanMedium {
        val type = when (tanMedium) {
            is TanGeneratorTanMedium -> TanMediumType.TanGeneratorTanMedium
            is MobilePhoneTanMedium -> TanMediumType.MobilePhoneTanMedium
            else -> TanMediumType.OtherTanMedium
        }

        return TanMedium(tanMedium.technicalId, bank.id, type, tanMedium.displayName, tanMedium.status,
            (tanMedium as? TanGeneratorTanMedium)?.cardNumber, (tanMedium as? MobilePhoneTanMedium)?.phoneNumber)
    }

    protected open fun map(tanMedium: TanMedium): net.dankito.banking.ui.model.tan.TanMedium {
        val displayName = tanMedium.displayName
        val status = tanMedium.status

        val mapped = when (tanMedium.type) {
            TanMediumType.TanGeneratorTanMedium -> TanGeneratorTanMedium(displayName, status, tanMedium.cardNumber ?: "")
            TanMediumType.MobilePhoneTanMedium -> MobilePhoneTanMedium(displayName, status, tanMedium.phoneNumber)
            else -> net.dankito.banking.ui.model.tan.TanMedium(displayName, status)
        }

        mapped.technicalId = tanMedium.id

        return mapped
    }


    override fun saveUrlToFile(url: String, file: File) {
        doSaveUrlToFile(url, file)
    }


    override fun findRemittees(query: String): List<Remittee> {
        return db.accountTransactionDao().findRemittees(query)
            .toSet() // don't display same Remittee multiple times
            .filterNot { it.bankCode.isNullOrBlank() || it.accountId.isNullOrBlank() }
            .map { Remittee(it.name, it.accountId, it.bankCode) }
    }

}
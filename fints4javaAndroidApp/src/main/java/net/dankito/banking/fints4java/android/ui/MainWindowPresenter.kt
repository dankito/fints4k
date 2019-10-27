package net.dankito.banking.fints4java.android.ui

import net.dankito.banking.fints4java.android.Base64ServiceAndroid
import net.dankito.fints.FinTsClient
import net.dankito.fints.FinTsClientCallback
import net.dankito.fints.banks.BankFinder
import net.dankito.fints.model.*
import net.dankito.fints.model.mapper.BankDataMapper
import net.dankito.fints.response.client.FinTsClientResponse
import net.dankito.fints.response.client.GetTransactionsResponse
import net.dankito.utils.IThreadPool
import net.dankito.utils.ThreadPool
import java.util.*
import kotlin.collections.ArrayList


open class MainWindowPresenter(callback: FinTsClientCallback) {

    protected val finTsClient = FinTsClient(callback, Base64ServiceAndroid())

    protected val bankFinder: BankFinder = BankFinder()

    protected val threadPool: IThreadPool = ThreadPool()

    protected val bankDataMapper = BankDataMapper()


    protected val accounts = mutableMapOf<CustomerData, BankData>()

    protected val bookedTransactions = mutableSetOf<AccountTransaction>() // TODO: map by account

    protected val unbookedTransactions = mutableSetOf<Any>()

    protected val accountAddedListeners = mutableListOf<(BankData, CustomerData) -> Unit>()


    open fun checkIfAccountExists(bankInfo: BankInfo, customerId: String, pin: String,
                                  callback: (FinTsClientResponse) -> Unit) {

        val bank = bankDataMapper.mapFromBankInfo(bankInfo)
        val customer = CustomerData(customerId, pin)

        finTsClient.checkIfAccountExistsAsync(bank, customer) { response ->
            if (response.isSuccessful) {
                accounts.put(customer, bank)

                callAccountAddedListeners(bank, customer)
            }

            callback(response)
        }
    }


    open fun getAccountTransactionsAsync(bank: BankData, customer: CustomerData,
                                         callback: (GetTransactionsResponse) -> Unit) {

        getAccountTransactionsAsync(bank, customer, null, callback)
    }

    open fun getAccountTransactionsAsync(bank: BankData, customer: CustomerData, fromDate: Date?,
                                                   callback: (GetTransactionsResponse) -> Unit) {

        finTsClient.getTransactionsAsync(GetTransactionsParameter(true, fromDate), bank, customer) { response ->
            if (response.isSuccessful) {
                bookedTransactions.addAll(response.bookedTransactions) // TODO: does currently not work, overwrite equals()
                unbookedTransactions.addAll(response.unbookedTransactions)
            }

            callback(response) // TODO: does not return all booked transactions, only the newly retrieved ones!
        }
    }

    open fun updateAccountsTransactionsAsync(callback: (GetTransactionsResponse) -> Unit) {
        val today = Date() // TODO: still don't know where this bug is coming from that bank returns a transaction dated at end of year
        val lastRetrievedTransactionDate = bookedTransactions.firstOrNull { it.bookingDate <= today }?.bookingDate // TODO: make multi-account ready; currently if don't differentiate booked transactions by accounts
        val fromDate = lastRetrievedTransactionDate?.let { Date(it.time - 24 * 60 * 60 * 1000) } // on day before last received transaction

        accounts.forEach { entry -> getAccountTransactionsAsync(entry.value, entry.key, fromDate, callback) } // TODO: this is not a good solution for multiple accounts
    }


    open fun searchForBankAsync(enteredBankCode: String, callback: (List<BankInfo>) -> Unit) {
        threadPool.runAsync {
            callback(searchForBank(enteredBankCode))
        }
    }

    open fun searchForBank(enteredBankCode: String): List<BankInfo> {
        return bankFinder.findBankByBankCode(enteredBankCode)
    }


    open fun searchAccountTransactions(query: String): List<AccountTransaction> {
        val queryLowercase = query.trim().toLowerCase()

        if (queryLowercase.isEmpty()) {
            return bookedTransactions.toList()
        }

        return bookedTransactions.filter {
            it.otherPartyName?.toLowerCase()?.contains(queryLowercase) == true
                    || it.usage.toLowerCase().contains(queryLowercase)
                    || it.bookingText?.toLowerCase()?.contains(queryLowercase) == true
        }
    }


    open fun addAccountAddedListener(listener: (BankData, CustomerData) -> Unit) {
        accountAddedListeners.add(listener)
    }

    protected open fun callAccountAddedListeners(bank: BankData, customer: CustomerData) {
        ArrayList(accountAddedListeners).forEach {
            it(bank, customer) // TODO: use RxJava for this
        }
    }

}
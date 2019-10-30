package net.dankito.banking.fints4java.android.ui

import net.dankito.banking.fints4java.android.Base64ServiceAndroid
import net.dankito.fints.FinTsClient
import net.dankito.fints.FinTsClientCallback
import net.dankito.fints.banks.BankFinder
import net.dankito.fints.model.*
import net.dankito.fints.model.mapper.BankDataMapper
import net.dankito.fints.response.client.AddAccountResponse
import net.dankito.fints.response.client.FinTsClientResponse
import net.dankito.fints.response.client.GetTransactionsResponse
import net.dankito.utils.IThreadPool
import net.dankito.utils.ThreadPool
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList


open class MainWindowPresenter(callback: FinTsClientCallback) {

    protected val finTsClient = FinTsClient(callback, Base64ServiceAndroid())

    protected val bankFinder: BankFinder = BankFinder()

    protected val threadPool: IThreadPool = ThreadPool()

    protected val bankDataMapper = BankDataMapper()


    protected val accounts = mutableMapOf<CustomerData, BankData>()

    protected val bookedTransactions = mutableMapOf<CustomerData, MutableSet<AccountTransaction>>()

    protected val unbookedTransactions = mutableMapOf<CustomerData, MutableSet<Any>>()

    protected val balances = mutableMapOf<CustomerData, BigDecimal>()

    protected val accountAddedListeners = mutableListOf<(BankData, CustomerData) -> Unit>()

    protected val retrievedAccountTransactionsResponseListeners = mutableListOf<(CustomerData, GetTransactionsResponse) -> Unit>()


    open fun addAccountAsync(bankInfo: BankInfo, customerId: String, pin: String,
                             callback: (AddAccountResponse) -> Unit) {

        val bank = bankDataMapper.mapFromBankInfo(bankInfo)
        val customer = CustomerData(customerId, pin)

        finTsClient.addAccountAsync(bank, customer) { response ->
            if (response.isSuccessful) {
                accounts.put(customer, bank)

                callAccountAddedListeners(bank, customer)

                if (response.supportsRetrievingTransactionsOfLast90DaysWithoutTan) {
                    retrievedAccountTransactions(customer, response)
                }
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
            retrievedAccountTransactions(customer, response)

            callback(response)
        }
    }

    open fun updateAccountsTransactionsAsync(callback: (GetTransactionsResponse) -> Unit) {
        accounts.forEach { entry ->
            val customer = entry.key
            val today = Date() // TODO: still don't know where this bug is coming from that bank returns a transaction dated at end of year
            val lastRetrievedTransactionDate = bookedTransactions[customer]?.firstOrNull { it.bookingDate <= today }?.bookingDate
            val fromDate = lastRetrievedTransactionDate?.let { Date(it.time - 24 * 60 * 60 * 1000) } // on day before last received transaction

            getAccountTransactionsAsync(entry.value, customer, fromDate, callback)
        }
    }

    protected open fun retrievedAccountTransactions(customer: CustomerData, response: GetTransactionsResponse) {
        if (response.isSuccessful) {
            updateAccountTransactionsAndBalances(customer, response)
        }

        callRetrievedAccountTransactionsResponseListener(customer, response)
    }

    protected open fun updateAccountTransactionsAndBalances(customer: CustomerData, response: GetTransactionsResponse) {

        if (bookedTransactions.containsKey(customer) == false) {
            bookedTransactions.put(customer, response.bookedTransactions.toMutableSet())
        }
        else {
            bookedTransactions[customer]?.addAll(response.bookedTransactions) // TODO: does currently not work, overwrite equals()
        }

        if (unbookedTransactions.containsKey(customer) == false) {
            unbookedTransactions.put(customer, response.unbookedTransactions.toMutableSet())
        }
        else {
            unbookedTransactions[customer]?.addAll(response.unbookedTransactions)
        }

        response.balance?.let {
            balances[customer] = it
        }
    }


    open fun transferMoneyAsync(bankTransferData: BankTransferData, callback: (FinTsClientResponse) -> Unit) {
        accounts.entries.firstOrNull()?.let {  // TODO: of course not correct, but i have to think of a multi account architecture and data model anyway
            finTsClient.doBankTransferAsync(bankTransferData, it.value, it.key, callback)
        }
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
            return bookedTransactions.values.flatten().toList()
        }

        return bookedTransactions.values.flatten().filter {
            it.otherPartyName?.toLowerCase()?.contains(queryLowercase) == true
                    || it.usage.toLowerCase().contains(queryLowercase)
                    || it.bookingText?.toLowerCase()?.contains(queryLowercase) == true
        }.sortedByDescending { it.bookingDate }
    }


    open val allTransactions: List<AccountTransaction>
        get() = bookedTransactions.values.flatten().toList() // TODO: someday add unbooked transactions

    open val balanceOfAllAccounts: BigDecimal
        get() = balances.values.fold(BigDecimal.ZERO) { acc, e -> acc + e }


    open fun addAccountAddedListener(listener: (BankData, CustomerData) -> Unit) {
        accountAddedListeners.add(listener)
    }

    protected open fun callAccountAddedListeners(bank: BankData, customer: CustomerData) {
        ArrayList(accountAddedListeners).forEach {
            it(bank, customer) // TODO: use RxJava for this
        }
    }

    open fun addRetrievedAccountTransactionsResponseListener(listener: (CustomerData, GetTransactionsResponse) -> Unit) {
        retrievedAccountTransactionsResponseListeners.add(listener)
    }

    protected open fun callRetrievedAccountTransactionsResponseListener(customer: CustomerData, response: GetTransactionsResponse) {
        ArrayList(retrievedAccountTransactionsResponseListeners).forEach {
            it(customer, response) // TODO: use RxJava for this
        }
    }

}
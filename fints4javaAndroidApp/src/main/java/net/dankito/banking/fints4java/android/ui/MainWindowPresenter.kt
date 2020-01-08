package net.dankito.banking.fints4java.android.ui

import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.IBankingClient
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.model.responses.GetTransactionsResponse
import net.dankito.banking.util.IBase64Service
import net.dankito.banking.util.UiCommonBase64ServiceWrapper
import net.dankito.fints.banks.BankFinder
import net.dankito.fints.model.BankInfo
import net.dankito.utils.IThreadPool
import net.dankito.utils.ThreadPool
import net.dankito.utils.web.client.OkHttpWebClient
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList


open class MainWindowPresenter(protected val base64Service: IBase64Service,
                               protected val callback: BankingClientCallback
) {

    companion object {
        protected const val OneDayMillis = 24 * 60 * 60 * 1000
    }


    protected val bankFinder: BankFinder = BankFinder()

    protected val threadPool: IThreadPool = ThreadPool()


    protected val clientsForAccounts = mutableMapOf<Account, IBankingClient>()

    protected val accountAddedListeners = mutableListOf<(Account) -> Unit>()

    protected val retrievedAccountTransactionsResponseListeners = mutableListOf<(BankAccount, GetTransactionsResponse) -> Unit>()


    // TODO: move BankInfo out of fints4javaLib
    open fun addAccountAsync(bankInfo: BankInfo, customerId: String, pin: String, callback: (AddAccountResponse) -> Unit) {

        val newClient = net.dankito.banking.fints4javaBankingClient(bankInfo, customerId, pin, OkHttpWebClient(), UiCommonBase64ServiceWrapper(base64Service), threadPool, this.callback)

        newClient.addAccountAsync { response ->
            val account = response.account

            if (response.isSuccessful) {
                clientsForAccounts.put(account, newClient)

                callAccountAddedListeners(account)

                if (response.supportsRetrievingTransactionsOfLast90DaysWithoutTan) {
                    account.bankAccounts.forEach { bankAccount ->
                        retrievedAccountTransactions(bankAccount, response)
                    }
                }
            }

            callback(response)
        }
    }


    open fun getAccountTransactionsAsync(account: Account,
                                         callback: (GetTransactionsResponse) -> Unit) {

        account.bankAccounts.forEach { bankAccount ->
            getAccountTransactionsAsync(bankAccount, callback) // TODO: use a synchronous version of getAccountTransactions() so that all bank accounts get handled serially
        }
    }

    open fun getAccountTransactionsAsync(bankAccount: BankAccount,
                                         callback: (GetTransactionsResponse) -> Unit) {

        getAccountTransactionsAsync(bankAccount, null, callback)
    }

    open fun getAccountTransactionsAsync(bankAccount: BankAccount, fromDate: Date?,
                                         callback: (GetTransactionsResponse) -> Unit) {

        getClientForAccount(bankAccount.account)?.let { client ->
            client.getTransactionsAsync(bankAccount, net.dankito.banking.ui.model.parameters.GetTransactionsParameter(true, fromDate)) { response ->

                retrievedAccountTransactions(bankAccount, response)

                callback(response)
            }
        }
    }

    open fun updateAccountsTransactionsAsync(callback: (GetTransactionsResponse) -> Unit) {
        clientsForAccounts.keys.forEach { account ->
            account.bankAccounts.forEach { bankAccount ->
                val today = Date() // TODO: still don't know where this bug is coming from that bank returns a transaction dated at end of year
                val lastRetrievedTransactionDate = bankAccount.bookedTransactions.firstOrNull { it.bookingDate <= today }?.bookingDate
                val fromDate = lastRetrievedTransactionDate?.let { Date(it.time - OneDayMillis) } // one day before last received transaction

                getAccountTransactionsAsync(bankAccount, fromDate, callback)
            }
        }
    }

    protected open fun retrievedAccountTransactions(bankAccount: BankAccount, response: GetTransactionsResponse) {
        if (response.isSuccessful) {
            updateAccountTransactionsAndBalances(bankAccount, response)
        }

        callRetrievedAccountTransactionsResponseListener(bankAccount, response)
    }

    protected open fun updateAccountTransactionsAndBalances(bankAccount: BankAccount, response: GetTransactionsResponse) {

        response.bookedTransactions.forEach { entry ->
            entry.key.addBookedTransactions(entry.value)
        }

        response.unbookedTransactions.forEach { entry ->
            entry.key.addUnbookedTransactions(entry.value)
        }

        response.balances.forEach { entry ->
            entry.key.balance = entry.value
        }
    }


    open fun transferMoneyAsync(bankAccount: BankAccount, data: TransferMoneyData, callback: (BankingClientResponse) -> Unit) {
        getClientForAccount(bankAccount.account)?.let { client ->
            client.transferMoneyAsync(data, callback)
        }
    }


    open fun preloadBanksAsync() {
        searchBanksByBankCodeAsync("1") { }
    }

    open fun searchBanksByBankCodeAsync(enteredBankCode: String, callback: (List<BankInfo>) -> Unit) {
        threadPool.runAsync {
            callback(searchBanksByBankCode(enteredBankCode))
        }
    }

    open fun searchBanksByBankCode(enteredBankCode: String): List<BankInfo> {
        return bankFinder.findBankByBankCode(enteredBankCode)
    }

    open fun searchBanksByNameBankCodeOrCity(query: String?): List<BankInfo> {
        if (query == null || query.isEmpty()) {
            return bankFinder.getBankList()
        }

        return bankFinder.findBankByNameBankCodeOrCity(query)
    }


    open fun searchAccountTransactions(query: String): List<AccountTransaction> {
        val queryLowercase = query.trim().toLowerCase()

        if (queryLowercase.isEmpty()) {
            return allTransactions
        }

        return allTransactions.filter {
            it.otherPartyName?.toLowerCase()?.contains(queryLowercase) == true
                    || it.usage.toLowerCase().contains(queryLowercase)
                    || it.bookingText?.toLowerCase()?.contains(queryLowercase) == true
        }
    }


    protected open fun getClientForAccount(account: Account): IBankingClient? {
        clientsForAccounts.get(account)?.let { client ->
            // TODO: is this code still needed after updating data model is implemented?
//            account.selectedTanProcedure?.let { selectedTanProcedure ->
//                client.customer.selectedTanProcedure = fints4javaModelMapper.mapTanProcedureBack(selectedTanProcedure)
//            }

            return client
        }

        return null
    }


    open val accounts: List<Account>
        get() = clientsForAccounts.keys.toList()

    open val allTransactions: List<AccountTransaction>
        get() = clientsForAccounts.keys.flatMap { it.transactions }.sortedByDescending { it.bookingDate } // TODO: someday add unbooked transactions

    open val balanceOfAllAccounts: BigDecimal
        get() = clientsForAccounts.keys.map { it.balance }.fold(BigDecimal.ZERO) { acc, e -> acc + e }


    open fun addAccountAddedListener(listener: (Account) -> Unit) {
        accountAddedListeners.add(listener)
    }

    protected open fun callAccountAddedListeners(account: Account) {
        ArrayList(accountAddedListeners).forEach {
            it(account) // TODO: use RxJava for this
        }
    }

    open fun addRetrievedAccountTransactionsResponseListener(listener: (BankAccount, GetTransactionsResponse) -> Unit) {
        retrievedAccountTransactionsResponseListeners.add(listener)
    }

    protected open fun callRetrievedAccountTransactionsResponseListener(bankAccount: BankAccount, response: GetTransactionsResponse) {
        ArrayList(retrievedAccountTransactionsResponseListeners).forEach {
            it(bankAccount, response) // TODO: use RxJava for this
        }
    }

}
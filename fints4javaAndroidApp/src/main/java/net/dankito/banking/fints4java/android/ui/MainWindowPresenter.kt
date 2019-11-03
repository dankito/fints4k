package net.dankito.banking.fints4java.android.ui

import net.dankito.banking.fints4java.android.Base64ServiceAndroid
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.model.responses.GetTransactionsResponse
import net.dankito.fints.FinTsClient
import net.dankito.fints.FinTsClientCallback
import net.dankito.fints.banks.BankFinder
import net.dankito.fints.model.*
import net.dankito.fints.model.mapper.BankDataMapper
import net.dankito.fints.response.client.FinTsClientResponse
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

    protected val fints4javaModelMapper = net.dankito.banking.fints4java.android.mapper.fints4javaModelMapper()


    protected val accounts = mutableMapOf<Account, Any>()

    protected val accountAddedListeners = mutableListOf<(Account) -> Unit>()

    protected val retrievedAccountTransactionsResponseListeners = mutableListOf<(BankAccount, GetTransactionsResponse) -> Unit>()


    open fun addAccountAsync(bankInfo: BankInfo, customerId: String, pin: String,
                             callback: (AddAccountResponse) -> Unit) {

        val bank = bankDataMapper.mapFromBankInfo(bankInfo)
        val customer = CustomerData(customerId, pin)

        finTsClient.addAccountAsync(bank, customer) { response ->
            val account = fints4javaModelMapper.mapAccount(customer, bank)
            val mappedResponse = fints4javaModelMapper.mapResponse(account, response)

            if (response.isSuccessful) {
                accounts.put(account, Pair(customer, bank))

                callAccountAddedListeners(account)

                if (response.supportsRetrievingTransactionsOfLast90DaysWithoutTan) {
                    account.bankAccounts.forEach { bankAccount ->
                        retrievedAccountTransactions(bankAccount, mappedResponse)
                    }
                }
            }

            callback(mappedResponse)
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

        getCustomerAndBankForAccount(bankAccount.account)?.let { customerBankPair ->
            finTsClient.getTransactionsAsync(GetTransactionsParameter(true, fromDate),
                customerBankPair.second, customerBankPair.first) { response ->

                val mappedResponse = fints4javaModelMapper.mapResponse(bankAccount.account, response)

                retrievedAccountTransactions(bankAccount, mappedResponse)

                callback(mappedResponse)
            }
        }
    }

    open fun updateAccountsTransactionsAsync(callback: (GetTransactionsResponse) -> Unit) {
        accounts.keys.forEach { account ->
            account.bankAccounts.forEach { bankAccount ->
                val today = Date() // TODO: still don't know where this bug is coming from that bank returns a transaction dated at end of year
                val lastRetrievedTransactionDate = bankAccount.bookedTransactions.firstOrNull { it.bookingDate <= today }?.bookingDate
                val fromDate = lastRetrievedTransactionDate?.let { Date(it.time - 24 * 60 * 60 * 1000) } // on day before last received transaction

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


    open fun transferMoneyAsync(bankAccount: BankAccount, bankTransferData: BankTransferData, callback: (FinTsClientResponse) -> Unit) {
        getCustomerAndBankForAccount(bankAccount.account)?.let { customerBankPair ->
            finTsClient.doBankTransferAsync(
                bankTransferData, customerBankPair.second, customerBankPair.first, callback)
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
            return allTransactions
        }

        return allTransactions.filter {
            it.otherPartyName?.toLowerCase()?.contains(queryLowercase) == true
                    || it.usage.toLowerCase().contains(queryLowercase)
                    || it.bookingText?.toLowerCase()?.contains(queryLowercase) == true
        }
    }


    protected open fun getCustomerAndBankForAccount(account: Account): Pair<CustomerData, BankData>? {
        (accounts.get(account) as? Pair<CustomerData, BankData>)?.let { customerBankPair ->
            account.selectedTanProcedure?.let { selectedTanProcedure ->
                customerBankPair.first.selectedTanProcedure = fints4javaModelMapper.mapTanProcedureBack(selectedTanProcedure)
            }

            return customerBankPair // TODO: return IBankingClient
        }

        return null
    }


    open val allTransactions: List<AccountTransaction>
        get() = accounts.keys.flatMap { it.transactions }.sortedByDescending { it.bookingDate } // TODO: someday add unbooked transactions

    open val balanceOfAllAccounts: BigDecimal
        get() = accounts.keys.map { it.balance }.fold(BigDecimal.ZERO) { acc, e -> acc + e }


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
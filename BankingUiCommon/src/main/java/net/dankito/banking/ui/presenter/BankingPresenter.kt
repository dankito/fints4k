package net.dankito.banking.ui.presenter

import net.dankito.banking.persistence.IBankingPersistence
import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.IBankingClient
import net.dankito.banking.ui.IBankingClientCreator
import net.dankito.banking.ui.IRouter
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.model.responses.GetTransactionsResponse
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium
import net.dankito.fints.banks.BankFinder
import net.dankito.fints.model.BankInfo
import net.dankito.utils.IThreadPool
import net.dankito.utils.ThreadPool
import net.dankito.utils.extensions.containsExactly
import net.dankito.utils.extensions.ofMaxLength
import org.slf4j.LoggerFactory
import java.io.File
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList


open class BankingPresenter(
    protected val bankingClientCreator: IBankingClientCreator,
    protected val dataFolder: File,
    protected val persister: IBankingPersistence,
    protected val router: IRouter,
    protected val threadPool: IThreadPool = ThreadPool()
) {

    companion object {
        protected const val OneDayMillis = 24 * 60 * 60 * 1000

        private val log = LoggerFactory.getLogger(BankingPresenter::class.java)
    }


    protected val bankFinder: BankFinder = BankFinder()


    protected val clientsForAccounts = mutableMapOf<Account, IBankingClient>()

    protected var selectedBankAccountsField = mutableListOf<BankAccount>()

    protected var userSelectedSingleAccount = false

    protected var saveAccountOnNextEnterTanInvocation = false


    protected val accountsChangedListeners = mutableListOf<(List<Account>) -> Unit>()

    protected val retrievedAccountTransactionsResponseListeners = mutableListOf<(BankAccount, GetTransactionsResponse) -> Unit>()

    protected val selectedBankAccountsChangedListeners = mutableListOf<(List<BankAccount>) -> Unit>()


    protected val callback: BankingClientCallback = object : BankingClientCallback {

        override fun enterTan(account: Account, tanChallenge: TanChallenge): EnterTanResult {
            if (saveAccountOnNextEnterTanInvocation) {
                persistAccount(account)
                saveAccountOnNextEnterTanInvocation = false
            }

            val result = router.getTanFromUserFromNonUiThread(account, tanChallenge, this@BankingPresenter)

            if (result.changeTanProcedureTo != null || result.changeTanMediumTo != null) { // then either selected TAN medium or procedure will change -> save account on next call to enterTan() as then changes will be visible
                saveAccountOnNextEnterTanInvocation = true
            }

            return result
        }

        override fun enterTanGeneratorAtc(tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
            return router.getAtcFromUserFromNonUiThread(tanMedium)
        }

    }


    init {
        threadPool.runAsync {
            readPersistedAccounts()
        }
    }


    protected open fun readPersistedAccounts() {
        try {
            dataFolder.mkdirs()

            val deserializedAccounts = persister.readPersistedAccounts()

            deserializedAccounts.forEach { account ->
                val bank = account.bank
                val bankInfo = BankInfo(bank.name, bank.bankCode, bank.bic, "", "", "", bank.finTsServerAddress, "FinTS V3.0", null)

                val newClient = bankingClientCreator.createClient(bankInfo, account.customerId, account.password,
                    dataFolder, threadPool, callback)

                try {
                    newClient.restoreData()
                } catch (e: Exception) {
                    log.error("Could not deserialize account data of $account", e)
                    // TODO: show error message to user
                }

                addClientForAccount(account, newClient)
            }

            callAccountsChangedListeners()

            selectedAllBankAccounts() // TODO: save last selected bank account(s)
        } catch (e: Exception) {
            log.error("Could not deserialize persisted accounts with persister $persister", e)
        }
    }

    protected open fun addClientForAccount(account: Account, client: IBankingClient) {
        clientsForAccounts.put(account, client)
    }


    // TODO: move BankInfo out of fints4javaLib
    open fun addAccountAsync(bankInfo: BankInfo, customerId: String, pin: String, callback: (AddAccountResponse) -> Unit) {

        val newClient = bankingClientCreator.createClient(bankInfo, customerId, pin, dataFolder, threadPool, this.callback)

        newClient.addAccountAsync { response ->
            val account = response.account

            if (response.isSuccessful) {
                addClientForAccount(account, newClient)

                selectedAccount(account)

                callAccountsChangedListeners()

                persistAccount(account)

                if (response.supportsRetrievingTransactionsOfLast90DaysWithoutTan) {
                    response.bookedTransactions.keys.forEach { bankAccount ->
                        retrievedAccountTransactions(bankAccount, response)
                    }
                }
            }

            callback(response)
        }
    }

    open fun deleteAccount(account: Account) {
        val wasSelected = isSingleSelectedAccount(account) or // either account or one of its bank accounts is currently selected
                (account.bankAccounts.firstOrNull { isSingleSelectedBankAccount(it) } != null)

        clientsForAccounts.remove(account)

        persister.deleteAccount(account, accounts)

        callAccountsChangedListeners()

        if (wasSelected) {
            selectedAllBankAccounts()
        }
    }


    open fun fetchAccountTransactionsAsync(account: Account,
                                           callback: (GetTransactionsResponse) -> Unit) {

        account.bankAccounts.forEach { bankAccount ->
            if (bankAccount.supportsRetrievingAccountTransactions) {
                fetchAccountTransactionsAsync(bankAccount, callback) // TODO: use a synchronous version of fetchAccountTransactions() so that all bank accounts get handled serially
            }
        }
    }

    open fun fetchAccountTransactionsAsync(bankAccount: BankAccount,
                                           callback: (GetTransactionsResponse) -> Unit) {

        fetchAccountTransactionsAsync(bankAccount, null, callback)
    }

    open fun fetchAccountTransactionsAsync(bankAccount: BankAccount, fromDate: Date?,
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
                if (bankAccount.supportsRetrievingAccountTransactions) {
                    val today = Date() // TODO: still don't know where this bug is coming from that bank returns a transaction dated at end of year
                    val lastRetrievedTransactionDate = bankAccount.bookedTransactions.firstOrNull { it.bookingDate <= today }?.bookingDate
                    val fromDate = lastRetrievedTransactionDate?.let { Date(it.time - OneDayMillis) } // one day before last received transaction

                    fetchAccountTransactionsAsync(bankAccount, fromDate, callback)
                }
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

        persistAccount(bankAccount.account)
    }

    protected open fun persistAccount(account: Account) {
        persister.saveOrUpdateAccount(account, accounts)
    }


    open fun transferMoneyAsync(bankAccount: BankAccount, data: TransferMoneyData, callback: (BankingClientResponse) -> Unit) {
        getClientForAccount(bankAccount.account)?.let { client ->
            client.transferMoneyAsync(data, bankAccount, callback)
        }
    }


    open fun preloadBanksAsync() {
        findUniqueBankForBankCodeAsync("1") { }
    }

    open fun findUniqueBankForIbanAsync(iban: String, callback: (BankInfo?) -> Unit) {
        threadPool.runAsync {
            callback(findUniqueBankForIban(iban))
        }
    }

    open fun findUniqueBankForIban(iban: String): BankInfo? {
        val ibanWithoutWhiteSpaces = iban.replace(" ", "")

        // first two characters are country code, 3rd and 4th character are checksum, bank code has 8 digits in Germany and user
        // should enter at least five characters before we start searching (before there shouldn't be a chance of a unique result)
        if (ibanWithoutWhiteSpaces.length >= 9) {
            if (ibanWithoutWhiteSpaces.startsWith("DE")) {
                return findUniqueBankForBankCode(ibanWithoutWhiteSpaces.substring(4).ofMaxLength(8))
            }
        }

        return null
    }

    open fun findUniqueBankForBankCodeAsync(bankCode: String, callback: (BankInfo?) -> Unit) {
        threadPool.runAsync {
            callback(findUniqueBankForBankCode(bankCode))
        }
    }

    open fun findUniqueBankForBankCode(bankCode: String): BankInfo? {
        val searchResult = bankFinder.findBankByBankCode(bankCode)

        val groupedByBic = searchResult.groupBy { it.bic }

        return if (groupedByBic.size == 1) searchResult.first() else null
    }

    open fun searchBanksByNameBankCodeOrCity(query: String?): List<BankInfo> {
        return bankFinder.findBankByNameBankCodeOrCity(query)
    }


    open fun searchSelectedAccountTransactions(query: String): List<AccountTransaction> {
        val queryLowercase = query.trim().toLowerCase()

        if (queryLowercase.isEmpty()) {
            return selectedBankAccountsAccountTransactions
        }

        return selectedBankAccountsAccountTransactions.filter {
            it.otherPartyName?.toLowerCase()?.contains(queryLowercase) == true
                    || it.usage.toLowerCase().contains(queryLowercase)
                    || it.bookingText?.toLowerCase()?.contains(queryLowercase) == true
        }
    }


    open fun showAddAccountDialog() {
        router.showAddAccountDialog(this)
    }

    open fun showTransferMoneyDialog(preselectedBankAccount: BankAccount? = null, preselectedValues: TransferMoneyData? = null) {
        router.showTransferMoneyDialog(this, preselectedBankAccount, preselectedValues)
    }


    protected open fun getClientForAccount(account: Account): IBankingClient? {
        return clientsForAccounts.get(account)
    }


    open val selectedBankAccounts: List<BankAccount>
        get() = ArrayList(selectedBankAccountsField)

    open val selectedBankAccountsAccountTransactions: List<AccountTransaction>
        get() = getAccountTransactionsForBankAccounts(selectedBankAccounts)

    open val balanceOfSelectedBankAccounts: BigDecimal
        get() = sumBalance(selectedBankAccounts.map { it.balance })

    open fun isSingleSelectedAccount(account: Account): Boolean {
        return userSelectedSingleAccount
                && selectedBankAccountsField.map { it.account }.toSet().containsExactly(account)
    }

    open fun isSingleSelectedBankAccount(bankAccount: BankAccount): Boolean {
        return userSelectedSingleAccount == false
                && selectedBankAccountsField.containsExactly(bankAccount)
    }

    open fun selectedAllBankAccounts() {
        userSelectedSingleAccount = false

        setSelectedBankAccounts(bankAccounts)
    }

    open fun selectedAccount(account: Account) {
        userSelectedSingleAccount = true

        setSelectedBankAccounts(account.bankAccounts)
    }

    open fun selectedBankAccount(bankAccount: BankAccount) {
        userSelectedSingleAccount = false

        setSelectedBankAccounts(listOf(bankAccount))
    }

    protected open fun setSelectedBankAccounts(bankAccounts: List<BankAccount>) {
        this.selectedBankAccountsField = ArrayList(bankAccounts) // make a copy

        callSelectedBankAccountsChangedListeners(selectedBankAccountsField)
    }


    open val accounts: List<Account>
        get() = clientsForAccounts.keys.toList()

    open val bankAccounts: List<BankAccount>
        get() = accounts.flatMap { it.bankAccounts }

    open val allTransactions: List<AccountTransaction>
        get() = getAccountTransactionsForBankAccounts(bankAccounts)

    open val balanceOfAllAccounts: BigDecimal
        get() = getBalanceForAccounts(accounts)


    open val bankAccountsSupportingRetrievingAccountTransactions: List<BankAccount>
        get() = bankAccounts.filter { it.supportsRetrievingAccountTransactions }

    open val hasBankAccountsSupportingRetrievingAccountTransactions: Boolean
        get() = doBankAccountsSupportRetrievingAccountTransactions(bankAccounts)

    open val doSelectedBankAccountsSupportRetrievingAccountTransactions: Boolean
        get() = doBankAccountsSupportRetrievingAccountTransactions(selectedBankAccounts)

    open fun doBankAccountsSupportRetrievingAccountTransactions(bankAccounts: List<BankAccount>): Boolean {
        return bankAccounts.firstOrNull { it.supportsRetrievingAccountTransactions } != null
    }


    open val bankAccountsSupportingRetrievingBalance: List<BankAccount>
        get() = bankAccounts.filter { it.supportsRetrievingBalance }

    open val hasBankAccountsSupportingRetrievingBalance: Boolean
        get() = doBankAccountsSupportRetrievingBalance(bankAccounts)

    open val doSelectedBankAccountsSupportRetrievingBalance: Boolean
        get() = doBankAccountsSupportRetrievingBalance(selectedBankAccounts)

    open fun doBankAccountsSupportRetrievingBalance(bankAccounts: List<BankAccount>): Boolean {
        return bankAccounts.firstOrNull { it.supportsRetrievingBalance } != null
    }


    open val bankAccountsSupportingTransferringMoney: List<BankAccount>
        get() = bankAccounts.filter { it.supportsTransferringMoney }

    open val hasBankAccountsSupportTransferringMoney: Boolean
        get() = doBankAccountsSupportTransferringMoney(bankAccounts)

    open val doSelectedBankAccountsSupportTransferringMoney: Boolean
        get() = doBankAccountsSupportTransferringMoney(selectedBankAccounts)

    open fun doBankAccountsSupportTransferringMoney(bankAccounts: List<BankAccount>): Boolean {
        return bankAccounts.firstOrNull { it.supportsTransferringMoney } != null
    }


    protected open fun getAccountTransactionsForBankAccounts(bankAccounts: Collection<BankAccount>): List<AccountTransaction> {
        return bankAccounts.flatMap { it.bookedTransactions }.sortedByDescending { it.bookingDate } // TODO: someday add unbooked transactions
    }

    protected open fun getBalanceForAccounts(accounts: Collection<Account>): BigDecimal {
        return accounts.map { it.balance }.fold(BigDecimal.ZERO) { acc, e -> acc + e }
    }

    protected open fun sumBalance(singleBalances: Collection<BigDecimal>): BigDecimal {
        return singleBalances.fold(BigDecimal.ZERO) { acc, e -> acc + e }
    }


    open fun addAccountsChangedListener(listener: (List<Account>) -> Unit): Boolean {
        return accountsChangedListeners.add(listener)
    }

    open fun removeAccountsChangedListener(listener: (List<Account>) -> Unit): Boolean {
        return accountsChangedListeners.add(listener)
    }

    protected open fun callAccountsChangedListeners() {
        val accounts = this.accounts

        ArrayList(accountsChangedListeners).forEach {
            it(accounts) // TODO: use RxJava for this
        }
    }


    open fun addRetrievedAccountTransactionsResponseListener(listener: (BankAccount, GetTransactionsResponse) -> Unit): Boolean {
        return retrievedAccountTransactionsResponseListeners.add(listener)
    }

    open fun removeRetrievedAccountTransactionsResponseListener(listener: (BankAccount, GetTransactionsResponse) -> Unit): Boolean {
        return retrievedAccountTransactionsResponseListeners.add(listener)
    }

    protected open fun callRetrievedAccountTransactionsResponseListener(bankAccount: BankAccount, response: GetTransactionsResponse) {
        ArrayList(retrievedAccountTransactionsResponseListeners).forEach {
            it(bankAccount, response) // TODO: use RxJava for this
        }
    }


    open fun addSelectedBankAccountsChangedListener(listener: (List<BankAccount>) -> Unit): Boolean {
        return selectedBankAccountsChangedListeners.add(listener)
    }

    open fun removeSelectedBankAccountsChangedListener(listener: (List<BankAccount>) -> Unit): Boolean {
        return selectedBankAccountsChangedListeners.add(listener)
    }

    protected open fun callSelectedBankAccountsChangedListeners(selectedBankAccounts: List<BankAccount>) {
        val selectedBankAccounts = this.selectedBankAccounts

        ArrayList(selectedBankAccountsChangedListeners).forEach {
            it(selectedBankAccounts) // TODO: use RxJava for this
        }
    }

}
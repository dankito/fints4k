package net.dankito.banking.fints.rest.service

import net.dankito.banking.bankfinder.InMemoryBankFinder
import net.dankito.banking.fints.FinTsClient
import net.dankito.banking.fints.callback.SimpleFinTsClientCallback
import net.dankito.banking.fints.model.*
import net.dankito.banking.fints.response.BankResponse
import net.dankito.banking.fints.response.client.AddAccountResponse
import net.dankito.banking.fints.response.client.GetTransactionsResponse
import net.dankito.banking.fints.rest.model.BankAccessData
import net.dankito.banking.fints.rest.model.EnterTanContext
import net.dankito.banking.fints.rest.model.EnteringTanRequested
import net.dankito.banking.fints.rest.model.dto.request.AccountRequestDto
import net.dankito.banking.fints.rest.model.dto.request.GetAccountsTransactionsRequestDto
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class fints4kService {

    protected val bankFinder = InMemoryBankFinder()

    protected val tanRequests = mutableMapOf<String, EnterTanContext>()


    fun getAddAccountResponse(accessData: BankAccessData): AddAccountResponse {
        val (bank, errorMessage) = mapToBankData(accessData)

        if (errorMessage != null) {
            return AddAccountResponse(BankResponse(false, errorMessage = errorMessage), bank)
        }

        return getAccountData(bank)
    }

    // TODO: as in most cases we really just want the account data, so just retrieve these without balances and transactions
    protected fun getAccountData(bank: BankData): AddAccountResponse {
        return getAsyncResponse { client, responseRetrieved ->
            client.addAccountAsync(bank) { response ->
                responseRetrieved(response)
            }
        }
    }


    fun getAccountTransactions(dto: GetAccountsTransactionsRequestDto): List<GetTransactionsResponse> {
        val (bank, errorMessage) = mapToBankData(dto.credentials)

        if (errorMessage != null) {
            return listOf(GetTransactionsResponse(BankResponse(false, errorMessage = errorMessage)))
        }

        val accountData = getAccountData(bank)

        return dto.accounts.map { accountDto ->
            val account = findAccount(accountData, accountDto)

            return@map if (account != null) {
                val parameter = GetTransactionsParameter(account, dto.alsoRetrieveBalance, dto.fromDate, dto.toDate, abortIfTanIsRequired = dto.abortIfTanIsRequired)
                getAccountTransactions(bank, parameter)
            }
            else {
                GetTransactionsResponse(BankResponse(false, errorMessage = "Account with identifier '${accountDto.identifier}' not found. Available accounts: ${accountData.bank.accounts.map { it.accountIdentifier }.joinToString(", ")}"))
            }
        }
    }

    fun getAccountTransactions(bank: BankData, parameter: GetTransactionsParameter): GetTransactionsResponse {
        return getAsyncResponse { client, responseRetrieved ->
            client.getTransactionsAsync(parameter, bank) { response ->
                responseRetrieved(response)
            }
        }
    }


    protected fun <T> getAsyncResponse(executeRequest: (FinTsClient, ((T) -> Unit)) -> Unit): T {
        val result = AtomicReference<T>()
        val countDownLatch = CountDownLatch(1)

//        val client = FinTsClient(SimpleFinTsClientCallback { supportedTanMethods: List<TanMethod>, suggestedTanMethod: TanMethod? ->
        val client = FinTsClient(SimpleFinTsClientCallback({ bank, tanChallenge -> handleEnterTan(bank, tanChallenge, countDownLatch, result) }) { supportedTanMethods, suggestedTanMethod ->
            suggestedTanMethod
        })

        executeRequest(client) { response ->
            result.set(response)
            countDownLatch.countDown()
        }

        countDownLatch.await()

        return result.get()
    }

    protected fun <T> handleEnterTan(bank: BankData, tanChallenge: TanChallenge, originatingRequestLatch: CountDownLatch, originatingRequestResult: AtomicReference<T>): EnterTanResult {
        val enterTanResult = AtomicReference<EnterTanResult>()
        val enterTanLatch = CountDownLatch(1)

        val tanRequestId = UUID.randomUUID().toString()

        originatingRequestResult.set(EnteringTanRequested(tanRequestId, bank, tanChallenge))
        originatingRequestLatch.countDown()

        tanRequests.put(tanRequestId, EnterTanContext(enterTanResult, enterTanLatch))

        enterTanLatch.await()

        return enterTanResult.get()
    }


    protected fun mapToBankData(accessData: BankAccessData): Pair<BankData, String?> {
        val bankSearchResult = bankFinder.findBankByBankCode(accessData.bankCode)
        val fintsServerAddress = accessData.finTsServerAddress ?: bankSearchResult.firstOrNull { it.pinTanAddress != null }?.pinTanAddress
        val bank = BankData(accessData.bankCode, accessData.loginName, accessData.password, fintsServerAddress ?: "",  bankSearchResult.firstOrNull()?.bic ?: "")

        if (fintsServerAddress == null) {
            val errorMessage = if (bankSearchResult.isEmpty()) "No bank found for bank code '${accessData.bankCode}'" else "Bank '${bankSearchResult.firstOrNull()?.name} does not support FinTS 3.0"

            return Pair(bank, errorMessage)
        }

        return Pair(bank, null)
    }

    protected fun findAccount(allAccounts: AddAccountResponse, accountDto: AccountRequestDto): AccountData? {
        return allAccounts.bank.accounts.firstOrNull { it.accountIdentifier == accountDto.identifier }
    }

}
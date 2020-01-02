package net.dankito.banking

import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.IBankingClient
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.parameters.GetTransactionsParameter
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.model.responses.GetTransactionsResponse
import net.dankito.fints.FinTsClientCallback
import net.dankito.fints.FinTsClientForCustomer
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.fints.model.*
import net.dankito.fints.model.mapper.BankDataMapper
import net.dankito.fints.util.IBase64Service
import net.dankito.utils.IThreadPool
import net.dankito.utils.ThreadPool
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.OkHttpWebClient


open class fints4javaBankingClient(
    bankInfo: BankInfo,
    customerId: String,
    pin: String,
    webClient: IWebClient = OkHttpWebClient(),
    base64Service: IBase64Service,
    threadPool: IThreadPool = ThreadPool(),
    callback: BankingClientCallback

) : IBankingClient {

    protected val mapper = net.dankito.banking.mapper.fints4javaModelMapper()

    protected val bankDataMapper = BankDataMapper()

    protected val bank = bankDataMapper.mapFromBankInfo(bankInfo)

    protected val customer = CustomerData(customerId, pin)

    protected lateinit var account: Account


    protected val client = FinTsClientForCustomer(bank, customer, webClient, base64Service, threadPool, object : FinTsClientCallback {
        override fun enterTan(customer: CustomerData, tanChallenge: TanChallenge): EnterTanResult {
            val result = callback.enterTan(account, mapper.mapTanChallenge(tanChallenge))

            return mapper.mapEnterTanResult(result, customer)
        }

        override fun enterTanGeneratorAtc(customer: CustomerData, tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
            val result = callback.enterTanGeneratorAtc(mapper.mapTanMedium(tanMedium))

            return mapper.mapEnterTanGeneratorAtcResult(result)
        }

    })


    override fun addAccountAsync(callback: (AddAccountResponse) -> Unit) {
        client.addAccountAsync { response ->
            this.account = mapper.mapAccount(customer, bank)
            val mappedResponse = mapper.mapResponse(account, response)

            callback(mappedResponse)
        }
    }

    override fun getTransactionsAsync(bankAccount: BankAccount, parameter: GetTransactionsParameter, callback: (GetTransactionsResponse) -> Unit) {
        client.getTransactionsAsync(net.dankito.fints.model.GetTransactionsParameter(parameter.alsoRetrieveBalance, parameter.fromDate, parameter.toDate)) { response ->

            val mappedResponse = mapper.mapResponse(bankAccount, response)

            callback(mappedResponse)
        }
    }

    override fun transferMoneyAsync(data: TransferMoneyData, callback: (BankingClientResponse) -> Unit) {
        val mappedData = BankTransferData(data.creditorName, data.creditorIban, data.creditorBic, data.amount, data.usage)

        client.doBankTransferAsync(mappedData) { response ->
            callback(mapper.mapResponse(response))
        }
    }
}
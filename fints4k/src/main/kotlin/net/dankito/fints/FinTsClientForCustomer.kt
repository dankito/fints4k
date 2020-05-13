package net.dankito.fints

import net.dankito.fints.callback.FinTsClientCallback
import net.dankito.fints.messages.MessageBuilder
import net.dankito.fints.model.*
import net.dankito.fints.response.ResponseParser
import net.dankito.fints.response.client.AddAccountResponse
import net.dankito.fints.response.client.FinTsClientResponse
import net.dankito.fints.response.client.GetTransactionsResponse
import net.dankito.fints.transactions.IAccountTransactionsParser
import net.dankito.fints.transactions.Mt940AccountTransactionsParser
import net.dankito.fints.util.IBase64Service
import net.dankito.utils.IThreadPool
import net.dankito.utils.ThreadPool
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.OkHttpWebClient


open class FinTsClientForCustomer @JvmOverloads constructor(
    val bank: BankData,
    val customer: CustomerData,
    webClient: IWebClient = OkHttpWebClient(),
    base64Service: IBase64Service,
    threadPool: IThreadPool = ThreadPool(),
    callback: FinTsClientCallback,
    messageBuilder: MessageBuilder = MessageBuilder(),
    responseParser: ResponseParser = ResponseParser(),
    mt940Parser: IAccountTransactionsParser = Mt940AccountTransactionsParser(),
    product: ProductData = ProductData("15E53C26816138699C7B6A3E8", "0.1") // TODO: get version dynamically
) {

    protected val client = FinTsClient(callback, base64Service, webClient, messageBuilder, responseParser, mt940Parser, threadPool, product)


    open fun addAccountAsync(callback: (AddAccountResponse) -> Unit) {
        client.addAccountAsync(bank, customer, callback)
    }

    open fun getTransactionsAsync(parameter: GetTransactionsParameter, account: AccountData, callback: (GetTransactionsResponse) -> Unit) {
        client.getTransactionsAsync(parameter, bank, customer, account, callback)
    }

    open fun doBankTransferAsync(bankTransferData: BankTransferData, account: AccountData, callback: (FinTsClientResponse) -> Unit) {
        client.doBankTransferAsync(bankTransferData, bank, customer, account, callback)
    }

}
package net.dankito.banking

import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.IBankingClient
import net.dankito.banking.ui.model.Customer
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.MessageLogEntry
import net.dankito.banking.ui.model.parameters.GetTransactionsParameter
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.model.responses.GetTransactionsResponse
import net.dankito.banking.fints.FinTsClientForCustomer
import net.dankito.banking.fints.callback.FinTsClientCallback
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.banking.fints.model.*
import net.dankito.banking.fints.util.IBase64Service
import net.dankito.banking.fints.util.PureKotlinBase64Service
import net.dankito.banking.fints.webclient.IWebClient
import net.dankito.banking.fints.webclient.KtorWebClient
import net.dankito.banking.extensions.toMoney
import net.dankito.banking.fints.response.client.FinTsClientResponse
import net.dankito.banking.util.ISerializer
import net.dankito.utils.multiplatform.File
import net.dankito.utils.multiplatform.log.LoggerFactory


open class fints4kBankingClient(
    protected val customer: Customer,
    protected val dataFolder: File,
    protected val serializer: ISerializer,
    webClient: IWebClient = KtorWebClient(),
    base64Service: IBase64Service = PureKotlinBase64Service(),
    callback: BankingClientCallback

) : IBankingClient {

    companion object {
        val fints4kClientDataFilename = "fints4kClientData.json"

        private val log = LoggerFactory.getLogger(fints4kBankingClient::class)
    }


    protected val mapper = net.dankito.banking.mapper.fints4kModelMapper()

    protected var didTryToGetAccountDataFromBank = false


    protected val bank = BankData(customer.bankCode, customer.finTsServerAddress, customer.bic, customer.bankName)

    protected val fints4kCustomer = CustomerData(customer.customerId, customer.password)


    protected open val client = FinTsClientForCustomer(bank, fints4kCustomer, createFinTsClientCallback(callback), webClient, base64Service)


    override val messageLogWithoutSensitiveData: List<MessageLogEntry>
        get() = client.messageLogWithoutSensitiveData.map { MessageLogEntry(it.message, it.time, customer) }


    override fun addAccountAsync(callback: (AddAccountResponse) -> Unit) {
        client.addAccountAsync { response ->
            handleAddAccountResponse(response, callback)
        }
    }

    protected open fun handleAddAccountResponse(response: net.dankito.banking.fints.response.client.AddAccountResponse,
                                                callback: (AddAccountResponse) -> Unit) {
        mapper.mapCustomer(customer, fints4kCustomer, bank)
        val mappedResponse = mapper.mapResponse(customer, response)

        saveData()

        callback(mappedResponse)
    }


    override fun getTransactionsAsync(bankAccount: BankAccount, parameter: GetTransactionsParameter, callback: (GetTransactionsResponse) -> Unit) {
        findAccountForBankAccount(bankAccount) { account, errorMessage ->
            if (account == null) {
                callback(GetTransactionsResponse(bankAccount, false, errorMessage))
            }
            else {
                val mappedParameter = GetTransactionsParameter(parameter.alsoRetrieveBalance, parameter.fromDate,
                    parameter.toDate, null, parameter.abortIfTanIsRequired,
                    { parameter.retrievedChunkListener?.invoke(mapper.mapTransactions(bankAccount, it)) } )

                doGetTransactionsAsync(mappedParameter, account, bankAccount, callback)
            }
        }
    }

    protected open fun doGetTransactionsAsync(parameter: net.dankito.banking.fints.model.GetTransactionsParameter,
                                              account: AccountData, bankAccount: BankAccount, callback: (GetTransactionsResponse) -> Unit) {
        client.getTransactionsAsync(parameter, account) { response ->
            handleGetTransactionsResponse(bankAccount, response, callback)
        }
    }

    protected open fun handleGetTransactionsResponse(bankAccount: BankAccount, response: net.dankito.banking.fints.response.client.GetTransactionsResponse,
                                                     callback: (GetTransactionsResponse) -> Unit) {
        val mappedResponse = mapper.mapResponse(bankAccount, response)

        saveData()

        callback(mappedResponse)
    }


    override fun transferMoneyAsync(data: TransferMoneyData, bankAccount: BankAccount, callback: (BankingClientResponse) -> Unit) {
        findAccountForBankAccount(bankAccount) { account, errorMessage ->
            if (account == null) {
                callback(BankingClientResponse(false, errorMessage))
            }
            else {
                val mappedData = BankTransferData(data.creditorName, data.creditorIban, data.creditorBic, data.amount.toMoney(), data.usage, data.instantPayment)

                doBankTransferAsync(mappedData, account, callback)
            }
        }
    }

    protected open fun doBankTransferAsync(data: BankTransferData, account: AccountData, callback: (BankingClientResponse) -> Unit) {
        client.doBankTransferAsync(data, account) { response ->
            handleBankTransferResponse(callback, response)
        }
    }

    protected open fun handleBankTransferResponse(callback: (BankingClientResponse) -> Unit, response: FinTsClientResponse) {
        saveData()

        callback(mapper.mapResponse(response))
    }


    protected open fun findAccountForBankAccount(bankAccount: BankAccount, findAccountResult: (AccountData?, error: String?) -> Unit) {
        val mappedAccount = mapper.findAccountForBankAccount(fints4kCustomer, bankAccount)

        if (mappedAccount != null) {
            findAccountResult(mappedAccount, null)
        }
        else if (didTryToGetAccountDataFromBank == false) { // then try to get account data by fetching data from bank
            addAccountAsync { response ->
                didTryToGetAccountDataFromBank = !!! response.isSuccessful

                findAccountResult(mapper.findAccountForBankAccount(fints4kCustomer, bankAccount),
                    response.errorToShowToUser)
            }
        }
        else {
            findAccountResult(null, "Cannot find account for ${bankAccount.identifier}") // TODO: translate
        }
    }


    override fun restoreData() {
        val deserializedCustomer = serializer.deserializeObject(getFints4kClientDataFile(), CustomerData::class)

        deserializedCustomer?.let {
            mapper.updateCustomer(fints4kCustomer, deserializedCustomer)

            mapper.mapCustomer(customer, fints4kCustomer, bank) // TODO: necessary?
        }
    }

    protected open fun saveData() {
        try {
            val clientDataFile = getFints4kClientDataFile()

            serializer.serializeObject(fints4kCustomer, clientDataFile)
        } catch (e: Exception) {
            log.error("Could not save customer data for $fints4kCustomer", e)
        }
    }

    protected open fun getFints4kClientDataFile(): File {
        val folder = File(dataFolder, "fints4k-client")

        folder.mkdirs()

        return File(folder, "${bank.bankCode}_${fints4kCustomer.customerId}_$fints4kClientDataFilename")
    }


    protected open fun createFinTsClientCallback(clientCallback: BankingClientCallback): FinTsClientCallback {
        return object : FinTsClientCallback {

            override fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>, suggestedTanProcedure: TanProcedure?, callback: (TanProcedure?) -> Unit) {
                handleAskUserForTanProcedure(supportedTanProcedures, suggestedTanProcedure, callback)
            }

            override fun enterTan(customer: CustomerData, tanChallenge: TanChallenge, callback: (EnterTanResult) -> Unit) {
                handleEnterTan(customer, tanChallenge, callback, clientCallback)
            }

            override fun enterTanGeneratorAtc(customer: CustomerData, tanMedium: TanGeneratorTanMedium, callback: (EnterTanGeneratorAtcResult) -> Unit) {
                handleEnterTanGeneratorAtc(customer, tanMedium, callback, clientCallback)
            }

        }
    }

    protected open fun handleAskUserForTanProcedure(supportedTanProcedures: List<TanProcedure>, suggestedTanProcedure: TanProcedure?, callback: (TanProcedure?) -> Unit) {
        // we simply return suggestedTanProcedure as even so it's not user's preferred TAN procedure she still can select it in EnterTanDialog
        callback(suggestedTanProcedure)
    }

    protected open fun handleEnterTan(customer: CustomerData, tanChallenge: TanChallenge, enterTanCallback: (EnterTanResult) -> Unit, clientCallback: BankingClientCallback) {
        mapper.updateTanMediaAndProcedures(this@fints4kBankingClient.customer, customer)

        clientCallback.enterTan(this@fints4kBankingClient.customer, mapper.mapTanChallenge(tanChallenge)) { result ->
            enterTanCallback(mapper.mapEnterTanResult(result, customer))
        }
    }

    protected open fun handleEnterTanGeneratorAtc(customer: CustomerData, tanMedium: TanGeneratorTanMedium, enterAtcCallback: (EnterTanGeneratorAtcResult) -> Unit, clientCallback: BankingClientCallback) {
        mapper.updateTanMediaAndProcedures(this@fints4kBankingClient.customer, customer)

        clientCallback.enterTanGeneratorAtc(mapper.mapTanMedium(tanMedium)) { result ->
            enterAtcCallback(mapper.mapEnterTanGeneratorAtcResult(result))
        }
    }

}
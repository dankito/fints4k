package net.dankito.banking

import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.IBankingClient
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
import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.MessageLogEntry
import net.dankito.banking.ui.model.mapper.IModelCreator
import net.dankito.banking.util.ISerializer
import net.dankito.utils.multiplatform.File
import net.dankito.utils.multiplatform.log.LoggerFactory


open class fints4kBankingClient(
    protected val customer: TypedCustomer,
    protected val modelCreator: IModelCreator,
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


    protected val mapper = net.dankito.banking.mapper.fints4kModelMapper(modelCreator)

    protected var didTryToGetAccountDataFromBank = false


    protected val bank = restoreDataOrMapFromUiModel(customer)


    protected open val client = FinTsClientForCustomer(bank, createFinTsClientCallback(callback), webClient, base64Service)


    override val messageLogWithoutSensitiveData: List<MessageLogEntry>
        get() = client.messageLogWithoutSensitiveData.map { MessageLogEntry(it.message, it.time, customer) }


    override fun addAccountAsync(callback: (AddAccountResponse) -> Unit) {
        client.addAccountAsync { response ->
            handleAddAccountResponse(response, callback)
        }
    }

    protected open fun handleAddAccountResponse(response: net.dankito.banking.fints.response.client.AddAccountResponse,
                                                callback: (AddAccountResponse) -> Unit) {
        mapper.mapBank(customer, bank)
        val mappedResponse = mapper.mapResponse(customer, response)

        saveData()

        callback(mappedResponse)
    }


    override fun getTransactionsAsync(bankAccount: TypedBankAccount, parameter: GetTransactionsParameter, callback: (GetTransactionsResponse) -> Unit) {
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
                                              account: AccountData, bankAccount: TypedBankAccount, callback: (GetTransactionsResponse) -> Unit) {
        client.getTransactionsAsync(parameter, account) { response ->
            handleGetTransactionsResponse(bankAccount, response, callback)
        }
    }

    protected open fun handleGetTransactionsResponse(bankAccount: TypedBankAccount, response: net.dankito.banking.fints.response.client.GetTransactionsResponse,
                                                     callback: (GetTransactionsResponse) -> Unit) {
        val mappedResponse = mapper.mapResponse(bankAccount, response)

        saveData()

        callback(mappedResponse)
    }


    override fun transferMoneyAsync(data: TransferMoneyData, callback: (BankingClientResponse) -> Unit) {
        findAccountForBankAccount(data.account) { account, errorMessage ->
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


    override fun dataChanged(customer: TypedCustomer) {
        mapper.mapChangesFromUiToClientModel(customer, bank)
    }

    override fun deletedAccount(customer: TypedCustomer, wasLastAccountWithThisCredentials: Boolean) {
        if (wasLastAccountWithThisCredentials) {
            getFints4kClientDataFile(customer).delete()
        }
    }


    protected open fun findAccountForBankAccount(bankAccount: TypedBankAccount, findAccountResult: (AccountData?, error: String?) -> Unit) {
        val mappedAccount = mapper.findAccountForBankAccount(bank, bankAccount)

        if (mappedAccount != null) {
            findAccountResult(mappedAccount, null)
        }
        else if (didTryToGetAccountDataFromBank == false) { // then try to get account data by fetching data from bank
            addAccountAsync { response ->
                didTryToGetAccountDataFromBank = !!! response.isSuccessful

                findAccountResult(mapper.findAccountForBankAccount(bank, bankAccount),
                    response.errorToShowToUser)
            }
        }
        else {
            findAccountResult(null, "Cannot find account for ${bankAccount.identifier}") // TODO: translate
        }
    }


    protected open fun restoreDataOrMapFromUiModel(customer: TypedCustomer): BankData {
        if (isNewAccount(customer)) {
            return mapToBankData(customer)
        }

        return restoreData(customer) ?: mapToBankData(customer)
    }

    protected open fun isNewAccount(customer: TypedCustomer): Boolean {
        return customer.accounts.isEmpty()
    }

    protected open fun mapToBankData(customer: TypedCustomer): BankData {
        return BankData(customer.bankCode, customer.customerId, customer.password, customer.finTsServerAddress, customer.bic, customer.bankName)
    }

    protected open fun restoreData(customer: TypedCustomer): BankData? {
        try {
            return serializer.deserializeObject(getFints4kClientDataFile(customer), BankData::class)
        } catch (e: Exception) {
            log.warn(e) { "Could not deserialize bank data of $customer (which is ok if bank is just about to be added)" }
        }

        return null
    }

    protected open fun saveData() {
        try {
            val clientDataFile = getFints4kClientDataFile(bank.bankCode, bank.customerId)

            serializer.serializeObject(bank, clientDataFile)
        } catch (e: Exception) {
            log.error("Could not save customer data for $bank", e)
        }
    }

    protected open fun getFints4kClientDataFile(customer: TypedCustomer): File {
        return getFints4kClientDataFile(customer.bankCode, customer.customerId)
    }

    protected open fun getFints4kClientDataFile(bankCode: String, customerId: String): File {
        val folder = File(dataFolder, "fints4k-client")

        folder.mkdirs()

        return File(folder, "${bankCode}_${customerId}_$fints4kClientDataFilename")
    }


    protected open fun createFinTsClientCallback(clientCallback: BankingClientCallback): FinTsClientCallback {
        return object : FinTsClientCallback {

            override fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>, suggestedTanProcedure: TanProcedure?, callback: (TanProcedure?) -> Unit) {
                handleAskUserForTanProcedure(supportedTanProcedures, suggestedTanProcedure, callback)
            }

            override fun enterTan(bank: BankData, tanChallenge: TanChallenge, callback: (EnterTanResult) -> Unit) {
                handleEnterTan(bank, tanChallenge, callback, clientCallback)
            }

            override fun enterTanGeneratorAtc(bank: BankData, tanMedium: TanGeneratorTanMedium, callback: (EnterTanGeneratorAtcResult) -> Unit) {
                handleEnterTanGeneratorAtc(bank, tanMedium, callback, clientCallback)
            }

        }
    }

    protected open fun handleAskUserForTanProcedure(supportedTanProcedures: List<TanProcedure>, suggestedTanProcedure: TanProcedure?, callback: (TanProcedure?) -> Unit) {
        // we simply return suggestedTanProcedure as even so it's not user's preferred TAN procedure she still can select it in EnterTanDialog
        callback(suggestedTanProcedure)
    }

    protected open fun handleEnterTan(bank: BankData, tanChallenge: TanChallenge, enterTanCallback: (EnterTanResult) -> Unit, clientCallback: BankingClientCallback) {
        mapper.updateTanMediaAndProcedures(this@fints4kBankingClient.customer, bank)

        clientCallback.enterTan(this@fints4kBankingClient.customer, mapper.mapTanChallenge(tanChallenge)) { result ->
            enterTanCallback(mapper.mapEnterTanResult(result, bank))
        }
    }

    protected open fun handleEnterTanGeneratorAtc(bank: BankData, tanMedium: TanGeneratorTanMedium, enterAtcCallback: (EnterTanGeneratorAtcResult) -> Unit, clientCallback: BankingClientCallback) {
        mapper.updateTanMediaAndProcedures(this@fints4kBankingClient.customer, bank)

        clientCallback.enterTanGeneratorAtc(mapper.mapTanMedium(tanMedium)) { result ->
            enterAtcCallback(mapper.mapEnterTanGeneratorAtcResult(result))
        }
    }

}
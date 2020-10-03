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
import net.dankito.banking.fints.model.BankData
import net.dankito.banking.fints.response.client.FinTsClientResponse
import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.MessageLogEntry
import net.dankito.banking.ui.model.mapper.IModelCreator
import net.dankito.banking.util.ISerializer
import net.dankito.utils.multiplatform.File
import net.dankito.utils.multiplatform.log.LoggerFactory


open class fints4kBankingClient(
    protected val bank: TypedBankData,
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


    protected val fintsBank = restoreDataOrMapFromUiModel(bank)


    protected open val client = FinTsClientForCustomer(fintsBank, createFinTsClientCallback(callback), webClient, base64Service)


    override val messageLogWithoutSensitiveData: List<MessageLogEntry>
        get() = client.messageLogWithoutSensitiveData.map { MessageLogEntry(it.message, it.time, bank) }


    override fun addAccountAsync(callback: (AddAccountResponse) -> Unit) {
        addAccountAsync(AddAccountParameter(fintsBank), callback)
    }

    protected open fun addAccountAsync(parameter: AddAccountParameter, callback: (AddAccountResponse) -> Unit) {
        client.addAccountAsync(parameter) { response ->
            handleAddAccountResponse(response, callback)
        }
    }

    protected open fun handleAddAccountResponse(response: net.dankito.banking.fints.response.client.AddAccountResponse,
                                                callback: (AddAccountResponse) -> Unit) {
        if (response.successful) { // if fintsBank couldn't be restored and then an error occurs, e.g. no network connection, then fintsBank contains almost no data which then gets mapped to bank -> accounts, TAN methods, TAN procedures, ... are lost
            mapper.mapBank(bank, fintsBank)
        }

        val mappedResponse = mapper.mapResponse(bank, response)

        saveData()

        callback(mappedResponse)
    }


    override fun getTransactionsAsync(parameter: GetTransactionsParameter, callback: (GetTransactionsResponse) -> Unit) {
        val account = parameter.account

        findAccountForAccount(account) { accountData, response ->
            if (accountData == null) {
                if (response != null) {
                    callback(GetTransactionsResponse(account, response))
                }
                else { // should never be the case
                    callback(GetTransactionsResponse(account, ""))
                }
            }
            else {
                val mappedParameter = GetTransactionsParameter(accountData, parameter.alsoRetrieveBalance, parameter.fromDate,
                    parameter.toDate, null, parameter.abortIfTanIsRequired) {
                        parameter.retrievedChunkListener?.invoke(mapper.mapTransactions(account, it))
                    }

                doGetTransactionsAsync(mappedParameter, account, callback)
            }
        }
    }

    protected open fun doGetTransactionsAsync(parameter: net.dankito.banking.fints.model.GetTransactionsParameter,
                                              account: TypedBankAccount, callback: (GetTransactionsResponse) -> Unit) {
        client.getTransactionsAsync(parameter) { response ->
            handleGetTransactionsResponse(account, response, callback)
        }
    }

    protected open fun handleGetTransactionsResponse(account: TypedBankAccount, response: net.dankito.banking.fints.response.client.GetTransactionsResponse,
                                                     callback: (GetTransactionsResponse) -> Unit) {
        val mappedResponse = mapper.mapResponse(account, response)

        saveData()

        callback(mappedResponse)
    }


    override fun transferMoneyAsync(data: TransferMoneyData, callback: (BankingClientResponse) -> Unit) {
        findAccountForAccount(data.account) { account, response ->
            if (account == null) {
                if (response != null) {
                    callback(response)
                }
                else { // should never be the case
                    callback(BankingClientResponse(false, "Konnte Kontodaten nicht vom Bankserver abrufen für ${data.account.identifier}. " +
                                "Besteht eine Netzwerkverbindung und sind der eingegebenen Benutzername und Passwort korrekt?")) // TODO: translate
                }
            }
            else {
                val mappedData = BankTransferData(data.recipientName, data.recipientAccountId, data.recipientBankCode, data.amount.toMoney(), data.reference, data.realTimeTransfer)

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


    override fun dataChanged(bank: TypedBankData) {
        mapper.mapChangesFromUiToClientModel(bank, this.fintsBank)
    }

    override fun deletedBank(bank: TypedBankData, wasLastAccountWithThisCredentials: Boolean) {
        if (wasLastAccountWithThisCredentials) {
            getFints4kClientDataFile(bank).delete()
        }
    }


    protected open fun findAccountForAccount(account: TypedBankAccount, findAccountResult: (AccountData?, BankingClientResponse?) -> Unit) {
        val mappedAccount = mapper.findMatchingAccount(fintsBank, account)

        if (mappedAccount != null) {
            findAccountResult(mappedAccount, null)
        }
        else { // then try to get account data by fetching data from bank
            addAccountAsync(AddAccountParameter(fintsBank, false)) { response ->
                if (response.successful) {
                    findAccountResult(mapper.findMatchingAccount(fintsBank, account), response)
                }
                else {
                    findAccountResult(null, response)
                }
            }
        }
    }


    protected open fun restoreDataOrMapFromUiModel(bank: TypedBankData): BankData {
        if (isNewAccount(bank)) {
            return mapToBankData(bank)
        }

        return restoreData(bank) ?: mapToBankData(bank)
    }

    protected open fun isNewAccount(bank: TypedBankData): Boolean {
        return bank.accounts.isEmpty()
    }

    protected open fun mapToBankData(bank: TypedBankData): BankData {
        return BankData(bank.bankCode, bank.userName, bank.password, bank.finTsServerAddress, bank.bic, bank.bankName)
    }

    protected open fun restoreData(bank: TypedBankData): BankData? {
        try {
            return serializer.deserializeObject(getFints4kClientDataFile(bank), BankData::class)
        } catch (e: Exception) {
            log.warn(e) { "Could not deserialize bank data of $bank" }
        }

        return null
    }

    protected open fun saveData() {
        try {
            val clientDataFile = getFints4kClientDataFile(fintsBank.bankCode, fintsBank.customerId)

            serializer.serializeObject(fintsBank, clientDataFile)
        } catch (e: Exception) {
            log.error("Could not save bank data for $fintsBank", e)
        }
    }

    protected open fun getFints4kClientDataFile(bank: TypedBankData): File {
        return getFints4kClientDataFile(bank.bankCode, bank.userName)
    }

    protected open fun getFints4kClientDataFile(bankCode: String, customerId: String): File {
        val folder = File(dataFolder, "fints4k-client")

        folder.mkdirs()

        return File(folder, "${bankCode}_${customerId}_$fints4kClientDataFilename")
    }


    protected open fun createFinTsClientCallback(clientCallback: BankingClientCallback): FinTsClientCallback {
        return object : FinTsClientCallback {

            override fun askUserForTanMethod(supportedTanMethods: List<TanMethod>, suggestedTanMethod: TanMethod?, callback: (TanMethod?) -> Unit) {
                handleAskUserForTanMethod(supportedTanMethods, suggestedTanMethod, callback)
            }

            override fun enterTan(bank: BankData, tanChallenge: TanChallenge, callback: (EnterTanResult) -> Unit) {
                handleEnterTan(bank, tanChallenge, callback, clientCallback)
            }

            override fun enterTanGeneratorAtc(bank: BankData, tanMedium: TanGeneratorTanMedium, callback: (EnterTanGeneratorAtcResult) -> Unit) {
                handleEnterTanGeneratorAtc(bank, tanMedium, callback, clientCallback)
            }

        }
    }

    protected open fun handleAskUserForTanMethod(supportedTanMethods: List<TanMethod>, suggestedTanMethod: TanMethod?, callback: (TanMethod?) -> Unit) {
        // we simply return suggestedTanProcedure as even so it's not user's preferred TAN procedure she still can select it in EnterTanDialog
        callback(suggestedTanMethod)
    }

    protected open fun handleEnterTan(bank: BankData, tanChallenge: TanChallenge, enterTanCallback: (EnterTanResult) -> Unit, clientCallback: BankingClientCallback) {
        mapper.updateTanMediaAndMethods(this@fints4kBankingClient.bank, bank)

        clientCallback.enterTan(this@fints4kBankingClient.bank, mapper.mapTanChallenge(tanChallenge)) { result ->
            enterTanCallback(mapper.mapEnterTanResult(result, bank))
        }
    }

    protected open fun handleEnterTanGeneratorAtc(bank: BankData, tanMedium: TanGeneratorTanMedium, enterAtcCallback: (EnterTanGeneratorAtcResult) -> Unit, clientCallback: BankingClientCallback) {
        mapper.updateTanMediaAndMethods(this@fints4kBankingClient.bank, bank)

        clientCallback.enterTanGeneratorAtc(mapper.mapTanMedium(tanMedium)) { result ->
            enterAtcCallback(mapper.mapEnterTanGeneratorAtcResult(result))
        }
    }

}
package net.dankito.banking

import net.dankito.banking.extensions.toIonspinBigDecimal
import net.dankito.banking.extensions.toJavaBigDecimal
import com.soywiz.klock.jvm.toDate
import net.dankito.banking.extensions.toKlockDate
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
import net.dankito.banking.mapper.BankDataMapper
import net.dankito.banking.fints.util.IBase64Service
import net.dankito.banking.fints.util.PureKotlinBase64Service
import net.dankito.utils.serialization.JacksonJsonSerializer
import net.dankito.banking.fints.webclient.IWebClient
import net.dankito.banking.fints.webclient.KtorWebClient
import net.dankito.banking.bankfinder.BankInfo
import org.slf4j.LoggerFactory
import java.io.File


open class fints4kBankingClient(
    bankInfo: BankInfo,
    customerId: String,
    pin: String,
    protected val dataFolder: File,
    webClient: IWebClient = KtorWebClient(),
    base64Service: IBase64Service = PureKotlinBase64Service(),
    callback: BankingClientCallback

) : IBankingClient {

    companion object {
        val fints4kClientDataFilename = "fints4kClientData.json"

        private val log = LoggerFactory.getLogger(fints4kBankingClient::class.java)


        init {
            net.dankito.banking.fints.util.log.LoggerFactory.loggerFactory = net.dankito.banking.fints.util.log.Slf4jLoggerFactory()
        }
    }


    protected val mapper = net.dankito.banking.mapper.fints4kModelMapper()

    protected val bankDataMapper = BankDataMapper()

    protected val serializer = JacksonJsonSerializer()


    protected val bank = bankDataMapper.mapFromBankInfo(bankInfo)

    protected val fints4kCustomer = CustomerData(customerId, pin)

    protected var customer: Customer = mapper.mapCustomer(fints4kCustomer, bank) // temporary save temp customer, we update with data from server response like BankAccounts later


    protected val client = FinTsClientForCustomer(bank, fints4kCustomer, object : FinTsClientCallback {

        override fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>, suggestedTanProcedure: TanProcedure?): TanProcedure? {
            // we simply return suggestedTanProcedure as even so it's not user's preferred TAN procedure she still can select it in EnterTanDialog
            return suggestedTanProcedure
        }

        override fun enterTan(customer: CustomerData, tanChallenge: TanChallenge): EnterTanResult {
            mapper.updateTanMediaAndProcedures(this@fints4kBankingClient.customer, customer)

            val result = callback.enterTan(this@fints4kBankingClient.customer, mapper.mapTanChallenge(tanChallenge))

            return mapper.mapEnterTanResult(result, customer)
        }

        override fun enterTanGeneratorAtc(customer: CustomerData, tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
            mapper.updateTanMediaAndProcedures(this@fints4kBankingClient.customer, customer)

            val result = callback.enterTanGeneratorAtc(mapper.mapTanMedium(tanMedium))

            return mapper.mapEnterTanGeneratorAtcResult(result)
        }

    }, webClient, base64Service)


    override val messageLogWithoutSensitiveData: List<MessageLogEntry>
        get() = client.messageLogWithoutSensitiveData.map { MessageLogEntry(it.message, it.time.toDate(), customer) }


    override fun addAccountAsync(callback: (AddAccountResponse) -> Unit) {
        client.addAccountAsync { response ->
            this.customer = mapper.mapCustomer(fints4kCustomer, bank)
            val mappedResponse = mapper.mapResponse(customer, response)

            saveData()

            callback(mappedResponse)
        }
    }

    override fun getTransactionsAsync(bankAccount: BankAccount, parameter: GetTransactionsParameter, callback: (GetTransactionsResponse) -> Unit) {
        val account = mapper.findAccountForBankAccount(fints4kCustomer, bankAccount)

        if (account == null) { // TODO: in this case retrieve data from bank, all data should be re-creatable
            callback(GetTransactionsResponse(bankAccount, false, "Cannot find account for ${bankAccount.identifier}")) // TODO: translate
        }
        else {
            client.getTransactionsAsync(GetTransactionsParameter(parameter.alsoRetrieveBalance, parameter.fromDate?.toKlockDate(), parameter.toDate?.toKlockDate(), null, parameter.abortIfTanIsRequired,
                { parameter.retrievedChunkListener?.invoke(mapper.mapTransactions(bankAccount, it)) } ), account) { response ->

                val mappedResponse = mapper.mapResponse(bankAccount, response)

                saveData()

                callback(mappedResponse)
            }
        }
    }

    override fun transferMoneyAsync(data: TransferMoneyData, bankAccount: BankAccount, callback: (BankingClientResponse) -> Unit) {
        val account = mapper.findAccountForBankAccount(fints4kCustomer, bankAccount)

        if (account == null) {
            callback(BankingClientResponse(false, "Cannot find account for ${bankAccount.identifier}")) // TODO: translate
        }
        else {
            val mappedData = BankTransferData(data.creditorName, data.creditorIban, data.creditorBic, data.amount.toIonspinBigDecimal(), data.usage, data.instantPayment)

            client.doBankTransferAsync(mappedData, account) { response ->
                saveData()

                callback(mapper.mapResponse(response))
            }
        }
    }


    override fun restoreData() {
        val deserializedCustomer = serializer.deserializeObject(getFints4kClientDataFile(), CustomerData::class.java)

        deserializedCustomer?.let {
            mapper.updateCustomer(fints4kCustomer, deserializedCustomer)

            customer = mapper.mapCustomer(fints4kCustomer, bank)
        }
    }

    protected open fun saveData() {
        try {
            val clientDataFile = getFints4kClientDataFile()

            clientDataFile.parentFile.mkdirs()

            serializer.serializeObject(fints4kCustomer, clientDataFile)
        } catch (e: Exception) {
            log.error("Could not save customer data for $fints4kCustomer", e)
        }
    }

    protected open fun getFints4kClientDataFile(): File {
        return File(File(dataFolder, "fints4k-client"), "${bank.bankCode}_${fints4kCustomer.customerId}_$fints4kClientDataFilename")
    }

}
import net.dankito.banking.fints.FinTsClient
import net.dankito.banking.fints.callback.FinTsClientCallback
import net.dankito.banking.fints.model.AccountData
import net.dankito.banking.fints.model.AccountFeature
import net.dankito.banking.fints.model.BankData
import net.dankito.banking.fints.model.CustomerData
import net.dankito.banking.fints.response.client.AddAccountResponse
import net.dankito.banking.fints.response.client.FinTsClientResponse
import net.dankito.banking.fints.response.client.GetTransactionsResponse
import net.dankito.banking.fints.webclient.IWebClient
import net.dankito.utils.multiplatform.toDate
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.freeze


open class FinTsClientWorkerArgument(val bank: BankData, val customer: CustomerData, val webClient: IWebClient, val callback: FinTsClientCallback)

class GetTransactionsWorkerArgument(val parameter: GetTransactionsParameter, val account: AccountData,
                                    bank: BankData, customer: CustomerData, webClient: IWebClient, callback: FinTsClientCallback)
    : FinTsClientWorkerArgument(bank, customer, webClient, callback)

data class FinTsClientWorkerResult<T : FinTsClientResponse>(
    val bank: BankData, val customer: CustomerData, val response: T)


open class FinTsClientForCustomerSwift(
    protected val bank: BankData,
    protected val customer: CustomerData,
    protected val webClient: IWebClient,
    protected val callback: FinTsClientCallback
) {

    init {
        webClient.freeze()
        callback.freeze()
    }


    protected val worker = Worker.start(name = "FinTsClient worker for $customer")


    open fun addAccountAsync(callback: (AddAccountResponse) -> Unit) {
        val future = worker.execute(TransferMode.SAFE, { createWorkerArgument() }) { arg ->
            val client = FinTsClient(arg.callback, arg.webClient)

            // unbelievable, i am not allowed to use the createCopy() methods
            val bank = BankData(
                arg.bank.bankCode, arg.bank.countryCode, arg.bank.finTs3ServerAddress, arg.bank.bic, arg.bank.bpdVersion,
                arg.bank.name, arg.bank.countMaxJobsPerMessage, arg.bank.supportedHbciVersions, arg.bank.supportedTanProcedures,
                arg.bank.changeTanMediumParameters, arg.bank.pinInfo, arg.bank.supportedLanguages, arg.bank.supportedJobs
            )
            val customer = CustomerData(
                arg.customer.customerId, arg.customer.pin, arg.customer.userId, arg.customer.name, arg.customer.updVersion,
                arg.customer.supportedTanProcedures, arg.customer.selectedTanProcedure, arg.customer.tanMedia,
                arg.customer.selectedLanguage, arg.customer.customerSystemId, arg.customer.customerSystemStatus
            )

            val response = client.addAccount(bank, customer)

            FinTsClientWorkerResult(bank.freeze(), customer.freeze(), response.freeze())
        }

        future.consume { result ->
            map(bank, result.bank)
            map(customer, result.customer)

            callback(result.response)
        }
    }


    open fun getTransactionsAsync(parameter: GetTransactionsParameter, account: AccountData, callback: (GetTransactionsResponse) -> Unit) {
        val future = worker.execute(TransferMode.SAFE, { createWorkerArgument(parameter, account) }) { arg ->
            val client = FinTsClient(arg.callback, arg.webClient)

            // unbelievable, i am not allowed to use the createCopy() methods
            val bank = BankData(
                arg.bank.bankCode, arg.bank.countryCode, arg.bank.finTs3ServerAddress, arg.bank.bic, arg.bank.bpdVersion,
                arg.bank.name, arg.bank.countMaxJobsPerMessage, arg.bank.supportedHbciVersions, arg.bank.supportedTanProcedures,
                arg.bank.changeTanMediumParameters, arg.bank.pinInfo, arg.bank.supportedLanguages, arg.bank.supportedJobs
            )
            val customer = CustomerData(
                arg.customer.customerId, arg.customer.pin, arg.customer.userId, arg.customer.name, arg.customer.updVersion,
                arg.customer.supportedTanProcedures, arg.customer.selectedTanProcedure, arg.customer.tanMedia,
                arg.customer.selectedLanguage, arg.customer.customerSystemId, arg.customer.customerSystemStatus
            )

            val response = client.getTransactions(arg.parameter, bank, customer, arg.account) // TODO: map account

            FinTsClientWorkerResult(bank.freeze(), customer.freeze(), response.freeze())
        }

        future.consume { result ->
            map(bank, result.bank)
            map(customer, result.customer)

            callback(result.response)
        }
    }


    protected open fun createWorkerArgument(): FinTsClientWorkerArgument {
        val bank = createImmutableCopy(this.bank)
        val customer = createImmutableCopy(this.customer)

        return FinTsClientWorkerArgument(bank, customer, webClient, callback)
    }

    protected open fun createWorkerArgument(parameter: GetTransactionsParameter, account: AccountData): GetTransactionsWorkerArgument {
        val bank = createImmutableCopy(this.bank)
        val customer = createImmutableCopy(this.customer)
        val frozenAccount = createImmutableCopy(account)

        return GetTransactionsWorkerArgument(parameter.freeze(), frozenAccount, bank, customer, webClient, callback)
    }


    protected open fun createCopy(bank: BankData): BankData {
        return BankData(
            bank.bankCode, bank.countryCode, bank.finTs3ServerAddress, bank.bic, bank.bpdVersion,
            bank.name, bank.countMaxJobsPerMessage, bank.supportedHbciVersions, bank.supportedTanProcedures,
            bank.changeTanMediumParameters, bank.pinInfo, bank.supportedLanguages, bank.supportedJobs
        )
    }

    protected open fun createImmutableCopy(bank: BankData): BankData {
        return createCopy(bank).freeze()
    }

    protected open fun createCopy(customer: CustomerData): CustomerData {
        return CustomerData(
            customer.customerId, customer.pin, customer.userId, customer.name, customer.updVersion,
            customer.supportedTanProcedures, customer.selectedTanProcedure, customer.tanMedia,
            customer.selectedLanguage, customer.customerSystemId, customer.customerSystemStatus
        )
    }

    protected open fun createImmutableCopy(customer: CustomerData): CustomerData {
        return createCopy(customer).freeze()
    }

    protected open fun createCopy(account: AccountData): AccountData {
        val copy = AccountData(
            account.accountIdentifier, account.subAccountAttribute, account.bankCountryCode,
            account.bankCode, account.iban, account.customerId, account.accountType, account.currency,
            account.accountHolderName, account.productName, account.accountLimit, account.allowedJobNames,
            account.allowedJobs, account.supportsRetrievingTransactionsOfLast90DaysWithoutTan,
            account.triedToRetrieveTransactionsOfLast90DaysWithoutTan
        )

        AccountFeature.values().forEach { feature ->
            copy.setSupportsFeature(feature, account.supportsFeature(feature))
        }

        return copy
    }

    protected open fun createImmutableCopy(account: AccountData): AccountData {
        return createCopy(account).freeze()
    }


    protected open fun map(to: BankData, from: BankData) {
        to.bankCode = from.bankCode
        to.countryCode = from.countryCode
        to.finTs3ServerAddress = from.finTs3ServerAddress
        to.bic = from.bic
        to.bpdVersion = from.bpdVersion
        to.name = from.name
        to.countMaxJobsPerMessage = from.countMaxJobsPerMessage
        to.supportedHbciVersions = from.supportedHbciVersions
        to.supportedTanProcedures = from.supportedTanProcedures
        to.changeTanMediumParameters = from.changeTanMediumParameters
        to.pinInfo = from.pinInfo
        to.supportedLanguages = from.supportedLanguages
        to.supportedJobs = from.supportedJobs
    }

    protected open fun map(to: CustomerData, from: CustomerData) {
        to.customerId = from.customerId
        to.pin = from.pin
        to.userId = from.userId
        to.name = from.name
        to.updVersion = from.updVersion
        to.supportedTanProcedures = from.supportedTanProcedures
        to.selectedTanProcedure = from.selectedTanProcedure
        to.tanMedia = from.tanMedia
        to.selectedLanguage = from.selectedLanguage
        to.customerSystemId = from.customerSystemId
        to.customerSystemStatus = from.customerSystemStatus

        // TODO: create convenience method to set accounts
        to.accounts.forEach { account ->
            to.removeAccount(account)
        }
        from.accounts.forEach { account ->
            to.addAccount(account) // TODO: also map account?
        }
    }

}
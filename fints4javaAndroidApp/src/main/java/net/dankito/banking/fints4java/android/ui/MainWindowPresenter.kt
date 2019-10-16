package net.dankito.banking.fints4java.android.ui

import net.dankito.banking.fints4java.android.Base64ServiceAndroid
import net.dankito.fints.FinTsClient
import net.dankito.fints.FinTsClientCallback
import net.dankito.fints.banks.BankFinder
import net.dankito.fints.model.*
import net.dankito.fints.model.mapper.BankDataMapper
import net.dankito.fints.response.client.FinTsClientResponse
import net.dankito.fints.response.client.GetTransactionsResponse
import net.dankito.utils.IThreadPool
import net.dankito.utils.ThreadPool


open class MainWindowPresenter {

    protected val callback = object : FinTsClientCallback {

        override fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>): TanProcedure? {
            // TODO: show dialog and ask user
            return supportedTanProcedures.first()
        }

        override fun enterTan(tanChallenge: TanChallenge): String? {
            return null
        }

    }

    protected val finTsClient = FinTsClient(callback, Base64ServiceAndroid())

    protected val bankFinder: BankFinder = BankFinder()

    protected val threadPool: IThreadPool = ThreadPool()

    protected val bankDataMapper = BankDataMapper()


    protected val accountAddedListeners = mutableListOf<(BankData, CustomerData) -> Unit>()


    open fun checkIfAccountExists(bankInfo: BankInfo, customerId: String, pin: String,
                                  callback: (FinTsClientResponse) -> Unit) {

        val bank = bankDataMapper.mapFromBankInfo(bankInfo)
        val customer = CustomerData(customerId, pin)

        finTsClient.checkIfAccountExistsAsync(bank, customer) { response ->
            if (response.isSuccessful) {
                callAccountAddedListeners(bank, customer)
            }

            callback(response)
        }
    }


    open fun getAccountTransactionsAsync(bank: BankData, customer: CustomerData,
                                                   callback: (GetTransactionsResponse) -> Unit) {

        finTsClient.tryGetTransactionsOfLast90DaysWithoutTanAsync(bank, customer, callback)
    }


    open fun searchForBankAsync(enteredBankCode: String, callback: (List<BankInfo>) -> Unit) {
        threadPool.runAsync {
            callback(searchForBank(enteredBankCode))
        }
    }

    open fun searchForBank(enteredBankCode: String): List<BankInfo> {
        return bankFinder.findBankByBankCode(enteredBankCode)
    }


    open fun addAccountAddedListener(listener: (BankData, CustomerData) -> Unit) {
        accountAddedListeners.add(listener)
    }

    protected open fun callAccountAddedListeners(bank: BankData, customer: CustomerData) {
        ArrayList(accountAddedListeners).forEach {
            it(bank, customer) // TODO: use RxJava for this
        }
    }

}
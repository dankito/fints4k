package net.dankito.banking.ui

import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.parameters.GetTransactionsParameter
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.model.responses.GetTransactionsResponse


interface IBankingClient {

    val messageLogWithoutSensitiveData: List<MessageLogEntry>


    fun addAccountAsync(callback: (AddAccountResponse) -> Unit)

    fun getTransactionsAsync(
        bankAccount: TypedBankAccount,
        parameter: GetTransactionsParameter,
        callback: (GetTransactionsResponse) -> Unit
    )

    fun transferMoneyAsync(data: TransferMoneyData, callback: (BankingClientResponse) -> Unit)


    fun dataChanged(customer: TypedCustomer)

}
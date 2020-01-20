package net.dankito.banking.ui

import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.parameters.GetTransactionsParameter
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.model.responses.GetTransactionsResponse


interface IBankingClient {

    fun addAccountAsync(callback: (AddAccountResponse) -> Unit)

    fun getTransactionsAsync(
        bankAccount: BankAccount,
        parameter: GetTransactionsParameter,
        callback: (GetTransactionsResponse) -> Unit
    )

    fun transferMoneyAsync(data: TransferMoneyData, bankAccount: BankAccount, callback: (BankingClientResponse) -> Unit)

}
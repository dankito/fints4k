package net.dankito.fints.transactions

import net.dankito.fints.model.AccountData
import net.dankito.fints.model.AccountTransaction


interface IAccountTransactionsParser {

    fun parseTransactions(transactionsString: String, account: AccountData): List<AccountTransaction>

}
package net.dankito.banking.fints.transactions

import net.dankito.banking.fints.model.AccountData
import net.dankito.banking.fints.model.AccountTransaction


interface IAccountTransactionsParser {

    fun parseTransactions(transactionsString: String, account: AccountData): List<AccountTransaction>

    fun parseTransactionsChunk(transactionsChunk: String, account: AccountData): Pair<List<AccountTransaction>, String>

}
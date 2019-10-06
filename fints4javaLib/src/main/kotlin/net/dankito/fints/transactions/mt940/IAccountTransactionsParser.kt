package net.dankito.fints.transactions.mt940

import net.dankito.fints.transactions.mt940.model.AccountStatement


interface IAccountTransactionsParser {

    fun parseTransactions(transactionsString: String): List<AccountStatement>

}
package net.dankito.banking.fints.transactions.mt940

import net.dankito.banking.fints.transactions.mt940.model.AccountStatement


interface IMt940Parser {

    fun parseMt940String(mt940String: String): List<AccountStatement>

}
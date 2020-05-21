package net.dankito.banking.fints.transactions.mt940

import net.dankito.banking.fints.transactions.mt940.model.AccountStatement


interface IMt940Parser {

    /**
     * Parses a whole MT 940 statements string, that is one that ends with a "-" line.
     */
    fun parseMt940String(mt940String: String): List<AccountStatement>

    /**
     * Parses incomplete MT 940 statements string, that is ones that not end with a "-" line,
     * as the they are returned e.g. if a HKKAZ response is dispersed over multiple messages.
     *
     * Tries to parse all statements in the string except an incomplete last one and returns an
     * incomplete last MT 940 statement (if any) as remainder.
     *
     * So each single HKKAZ partial response can be parsed immediately, its statements/transactions
     * be displayed immediately to user and the remainder then be passed together with next partial
     * HKKAZ response to this method till this whole MT 940 statement is parsed.
     */
    fun parseMt940Chunk(mt940Chunk: String): Pair<List<AccountStatement>, String>

}
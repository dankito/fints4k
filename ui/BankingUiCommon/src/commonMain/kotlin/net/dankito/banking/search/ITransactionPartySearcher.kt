package net.dankito.banking.search


interface ITransactionPartySearcher {

    fun findTransactionParty(query: String): List<TransactionParty>

}
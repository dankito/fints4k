package net.dankito.banking.search


open class NoOpTransactionPartySearcher : ITransactionPartySearcher {

    override fun findTransactionParty(query: String): List<TransactionParty> {
        return listOf()
    }

}
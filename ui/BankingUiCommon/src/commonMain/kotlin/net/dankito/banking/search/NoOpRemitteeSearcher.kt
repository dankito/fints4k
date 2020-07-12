package net.dankito.banking.search


open class NoOpRemitteeSearcher : IRemitteeSearcher {

    override fun findRemittees(query: String): List<Remittee> {
        return listOf()
    }

}
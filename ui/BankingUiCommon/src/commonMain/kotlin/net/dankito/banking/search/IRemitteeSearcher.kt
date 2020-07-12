package net.dankito.banking.search


interface IRemitteeSearcher {

    fun findRemittees(query: String): List<Remittee>

}
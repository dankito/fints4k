package net.dankito.banking.search

import net.dankito.banking.LuceneConfig
import net.dankito.banking.LuceneConfig.Companion.OtherPartyNameFieldName
import net.dankito.utils.lucene.cache.MapBasedCache
import net.dankito.utils.lucene.search.MapCachedSearchConfig
import net.dankito.utils.lucene.search.QueryBuilder
import net.dankito.utils.lucene.search.Searcher
import java.io.File


open class LuceneRemitteeSearcher(indexFolder: File) : IRemitteeSearcher {


    protected val queries = QueryBuilder()

    protected val searcher = Searcher(LuceneConfig.getAccountTransactionsIndexFolder(indexFolder))

    protected val cache = MapBasedCache<String>()


    override fun findRemittees(query: String): List<Remittee> {
        val luceneQuery = queries.createQueriesForSingleTerms(query.toLowerCase()) { singleTerm ->
            listOf(
                queries.fulltextQuery(OtherPartyNameFieldName, singleTerm)
            )
        }

        return searcher.searchAndDeserialize(MapCachedSearchConfig(luceneQuery, PersistedRemittee::class.java, listOf(), cache))
            .map { Remittee(it.otherPartyName ?: "", it.otherPartyAccountId, it.otherPartyBankCode) }
            .toSet() // don't display same Remittee multiple times
            .filterNot { it.iban.isNullOrBlank() || it.bic.isNullOrBlank() } // e.g. comdirect doesn't supply other party's IBAN and BIC -> filter these as they have no value for auto-entering a remittee's IBAN and BIC
    }

}
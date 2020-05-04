package net.dankito.banking.search

import net.dankito.banking.LuceneConfig
import net.dankito.banking.LuceneConfig.Companion.OtherPartyAccountIdFieldName
import net.dankito.banking.LuceneConfig.Companion.OtherPartyBankCodeFieldName
import net.dankito.banking.LuceneConfig.Companion.OtherPartyNameFieldName
import net.dankito.utils.lucene.mapper.PropertyDescription
import net.dankito.utils.lucene.mapper.PropertyType
import net.dankito.utils.lucene.search.MappedSearchConfig
import net.dankito.utils.lucene.search.QueryBuilder
import net.dankito.utils.lucene.search.Searcher
import java.io.File


open class LuceneRemitteeSearcher(indexFolder: File) : IRemitteeSearcher {

    companion object {

        private val properties = listOf(
            PropertyDescription(PropertyType.NullableString, OtherPartyNameFieldName, Remittee::name),
            PropertyDescription(PropertyType.NullableString, OtherPartyBankCodeFieldName, Remittee::bic),
            PropertyDescription(PropertyType.NullableString, OtherPartyAccountIdFieldName, Remittee::iban)
        )

    }


    protected val queries = QueryBuilder()

    protected val searcher = Searcher(LuceneConfig.getAccountTransactionsIndexFolder(indexFolder))


    override fun findRemittees(query: String): List<Remittee> {
        val luceneQuery = queries.createQueriesForSingleTerms(query.toLowerCase()) { singleTerm ->
            listOf(
                queries.fulltextQuery(OtherPartyNameFieldName, singleTerm)
            )
        }

        return searcher.searchAndMap(MappedSearchConfig(luceneQuery, Remittee::class.java, properties)).toSet().toList()
    }

}
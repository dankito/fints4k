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


open class LuceneTransactionPartySearcher(indexFolder: File) : ITransactionPartySearcher {

    companion object {

        private val properties = listOf(
            PropertyDescription(PropertyType.NullableString, OtherPartyNameFieldName, TransactionParty::name),
            PropertyDescription(PropertyType.NullableString, OtherPartyBankCodeFieldName, TransactionParty::bic),
            PropertyDescription(PropertyType.NullableString, OtherPartyAccountIdFieldName, TransactionParty::iban)
        )

    }


    protected val queries = QueryBuilder()

    protected val searcher = Searcher(LuceneConfig.getAccountTransactionsIndexFolder(indexFolder))


    override fun findTransactionParty(query: String): List<TransactionParty> {
        val luceneQuery = queries.createQueriesForSingleTerms(query.toLowerCase()) { singleTerm ->
            listOf(
                queries.fulltextQuery(OtherPartyNameFieldName, singleTerm)
            )
        }

        return searcher.searchAndMap(MappedSearchConfig(luceneQuery, TransactionParty::class.java, properties))
            .toSet() // don't display same transaction party multiple times
            .filterNot { it.iban.isNullOrBlank() || it.bic.isNullOrBlank() } // e.g. comdirect doesn't supply other party's IBAN and BIC -> filter these as they have no value for auto-entering a transaction party's IBAN and BIC
    }

}
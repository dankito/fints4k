package net.dankito.banking.fints.banks

import net.dankito.banking.fints.model.BankInfo
import net.dankito.utils.Stopwatch
import net.dankito.utils.hashing.HashAlgorithm
import net.dankito.utils.hashing.HashService
import net.dankito.utils.io.FileUtils
import net.dankito.utils.lucene.Constants
import net.dankito.utils.lucene.cache.MapBasedCache
import net.dankito.utils.lucene.index.DocumentsWriter
import net.dankito.utils.lucene.index.FieldBuilder
import net.dankito.utils.lucene.mapper.PropertyDescription
import net.dankito.utils.lucene.mapper.PropertyType
import net.dankito.utils.lucene.search.*
import org.apache.lucene.document.Document
import org.apache.lucene.search.Query
import org.slf4j.LoggerFactory
import java.io.File


open class LuceneBankFinder(indexFolder: File) : BankFinderBase(), IBankFinder {

    companion object {

        const val IndexedBankListFileHashIdFieldName = "IndexedBankListFileHashId"
        const val IndexedBankListFileHashIdFieldValue = "IndexedBankListFileHashValue"
        const val IndexedBankListFileHashFieldName = "IndexedBankListFileHash"

        const val BankInfoNameFieldName = "name"
        const val BankInfoBankCodeFieldName = "bank_code"
        const val BankInfoBicFieldName = "bic"
        const val BankInfoCityIndexedFieldName = "city_indexed"
        const val BankInfoCityStoredFieldName = "city_stored"
        const val BankInfoPostalCodeFieldName = "postal_code"
        const val BankInfoChecksumMethodFieldName = "checksum_method"
        const val BankInfoPinTanServerAddressFieldName = "pin_tan_server_address"
        const val BankInfoPinTanVersionFieldName = "pin_tan_version"
        const val BankInfoOldBankCodeFieldName = "old_bank_code"

        val bankInfoProperties = listOf(
            PropertyDescription(PropertyType.String, BankInfoNameFieldName, BankInfo::name),
            PropertyDescription(PropertyType.String, BankInfoBankCodeFieldName, BankInfo::bankCode),
            PropertyDescription(PropertyType.String, BankInfoBicFieldName, BankInfo::bic),
            PropertyDescription(PropertyType.String, BankInfoPostalCodeFieldName, BankInfo::postalCode),
            PropertyDescription(PropertyType.String, BankInfoCityStoredFieldName, BankInfo::city),
            PropertyDescription(PropertyType.String, BankInfoChecksumMethodFieldName, BankInfo::checksumMethod),
            PropertyDescription(PropertyType.NullableString, BankInfoPinTanServerAddressFieldName, BankInfo::pinTanAddress),
            PropertyDescription(PropertyType.NullableString, BankInfoPinTanVersionFieldName, BankInfo::pinTanVersion),
            PropertyDescription(PropertyType.NullableString, BankInfoOldBankCodeFieldName, BankInfo::oldBankCode)
        )


        private val log = LoggerFactory.getLogger(LuceneBankFinder::class.java)

    }


    protected val indexDir = File(indexFolder, "banklist")


    protected val fileUtils = FileUtils()

    protected val hashService = HashService(fileUtils)


    protected val fields = FieldBuilder()


    protected val queries = QueryBuilder()

    protected val mapper = FieldMapper()

    protected val searcher = Searcher(indexDir)


    protected var bankFinderWhileUpdatingIndex: IBankFinder? = null


    override fun findBankByBankCode(query: String): List<BankInfo> {
        bankFinderWhileUpdatingIndex?.let {
            return it.findBankByBankCode(query)
        }

        if (query.isBlank()) {
            return getBankList()
        }

        val luceneQuery = queries.startsWith(BankInfoBankCodeFieldName, query)

        return getBanksFromQuery(luceneQuery)
    }

    override fun findBankByNameOrCityForNonEmptyQuery(query: String): List<BankInfo> {
        bankFinderWhileUpdatingIndex?.let {
            return it.findBankByNameBankCodeOrCity(query)
        }

        val luceneQuery = queries.createQueriesForSingleTerms(query.toLowerCase()) { singleTerm ->
            listOf(
                queries.fulltextQuery(BankInfoNameFieldName, singleTerm),
                queries.contains(BankInfoCityIndexedFieldName, singleTerm)
            )
        }

        return getBanksFromQuery(luceneQuery)
    }

    override fun getBankList(): List<BankInfo> {
        bankFinderWhileUpdatingIndex?.let {
            return it.getBankList()
        }

        return getBanksFromQuery(queries.allDocumentsThatHaveField(BankInfoNameFieldName))
    }

    protected open fun getBanksFromQuery(query: Query): List<BankInfo> {
        // there are more than 16.000 banks in bank list -> 10.000 is too few
        return searcher.searchAndDeserializeLazily(MappedSearchConfig(query, BankInfo::class.java, listOf(), 100_000))
    }


    override fun preloadBankList() {
        val hashSearchResult = searcher.search(
            queries.exact(IndexedBankListFileHashIdFieldName, IndexedBankListFileHashIdFieldValue, false))

        val lastIndexedBankListFileHash = hashSearchResult.hits.firstOrNull()?.let {
            mapper.string(it, IndexedBankListFileHashFieldName)
        }

        if (lastIndexedBankListFileHash == null) {
            updateIndex()
        }
        else {
            val currentBankListFileHash = calculateCurrentBankListFileHash()

            if (currentBankListFileHash != lastIndexedBankListFileHash) {
                updateIndex(currentBankListFileHash)
            }
        }
    }

    protected open fun updateIndex() {
        updateIndex(calculateCurrentBankListFileHash())
    }

    protected open fun updateIndex(bankListFileHash: String) {
        try {
            val banks = loadBankListFile()

            // while indexing - which takes a long time on Android - use InMemoryBankFinder so that user sees at least some search results even though it's slower
            bankFinderWhileUpdatingIndex = InMemoryBankFinder(banks)

            fileUtils.deleteFolderRecursively(indexDir) // delete current index
            indexDir.mkdirs()

            writeBanksToIndex(banks, bankListFileHash)

            bankFinderWhileUpdatingIndex = null // now use LuceneBankFinder again for searching
        } catch (e: Exception) {
            log.error("Could not update index", e)
        }
    }

    protected open fun writeBanksToIndex(banks: List<BankInfo>, bankListFileHash: String) {
        DocumentsWriter(indexDir).use { writer ->
            banks.forEach { bank ->
                writer.saveDocumentForObject(bank, listOf(
                    fields.fullTextSearchField(BankInfoNameFieldName, bank.name),
                    fields.keywordField(BankInfoBankCodeFieldName, bank.bankCode),
                    fields.fullTextSearchField(BankInfoCityIndexedFieldName, bank.city)
                ))
            }

            writer.updateDocument(IndexedBankListFileHashIdFieldName, IndexedBankListFileHashIdFieldValue,
                fields.storedField(IndexedBankListFileHashFieldName, bankListFileHash))

            writer.optimizeIndex()
        }
    }


    protected open fun calculateCurrentBankListFileHash(): String {
        return calculateHash(readBankListFile())
    }

    protected open fun calculateHash(stringToHash: String): String {
        return hashService.hashString(HashAlgorithm.SHA512, stringToHash)
    }

}
package net.dankito.fints.banks

import net.dankito.fints.model.BankInfo
import net.dankito.utils.hashing.HashAlgorithm
import net.dankito.utils.hashing.HashService
import net.dankito.utils.io.FileUtils
import net.dankito.utils.lucene.index.DocumentsWriter
import net.dankito.utils.lucene.index.FieldBuilder
import net.dankito.utils.lucene.search.FieldMapper
import net.dankito.utils.lucene.search.QueryBuilder
import net.dankito.utils.lucene.search.Searcher
import org.apache.lucene.document.Document
import org.apache.lucene.search.Query
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

    }


    protected val indexDir = File(indexFolder, "banklist")


    protected val fileUtils = FileUtils()

    protected val hashService = HashService(fileUtils)


    protected val fields = FieldBuilder()


    protected val queries = QueryBuilder()

    protected val mapper = FieldMapper()

    protected val searcher = Searcher(indexDir)


    override fun findBankByBankCode(query: String): List<BankInfo> {
        if (query.isBlank()) {
            return getBankList()
        }

        val luceneQuery = queries.startsWith(BankInfoBankCodeFieldName, query)

        return getBanksFromQuery(luceneQuery)
    }

    override fun findBankByNameBankCodeOrCity(query: String?): List<BankInfo> {
        if (query.isNullOrBlank()) {
            return getBankList()
        }

        val luceneQuery = queries.createQueriesForSingleTerms(query.toLowerCase()) { singleTerm ->
            listOf(
                queries.fulltextQuery(BankInfoNameFieldName, singleTerm),
                queries.startsWith(BankInfoBankCodeFieldName, singleTerm),
                queries.contains(BankInfoCityIndexedFieldName, singleTerm)
            )
        }

        return getBanksFromQuery(luceneQuery)
    }

    override fun getBankList(): List<BankInfo> {
        return getBanksFromQuery(queries.allDocumentsThatHaveField(BankInfoNameFieldName))
    }

    protected fun getBanksFromQuery(query: Query): List<BankInfo> {
        val results = searcher.search(query, 100_000) // there are more than 16.000 banks in bank list -> 10.000 is too few

        return results.hits.map { result ->
            BankInfo(
                mapper.string(result, BankInfoNameFieldName),
                mapper.string(result, BankInfoBankCodeFieldName),
                mapper.string(result, BankInfoBicFieldName),
                mapper.string(result, BankInfoPostalCodeFieldName),
                mapper.string(result, BankInfoCityStoredFieldName),
                mapper.string(result, BankInfoChecksumMethodFieldName),
                mapper.nullableString(result, BankInfoPinTanServerAddressFieldName),
                mapper.nullableString(result, BankInfoPinTanVersionFieldName),
                mapper.nullableString(result, BankInfoOldBankCodeFieldName)
            )
        }
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
        fileUtils.deleteFolderRecursively(indexDir)
        indexDir.mkdirs()

        DocumentsWriter(indexDir).use { writer ->
            val banks = loadBankListFile()

            writer.saveDocuments(banks.map {
                createDocumentForBank(it, writer)
            } )

            writer.updateDocument(IndexedBankListFileHashIdFieldName, IndexedBankListFileHashIdFieldValue,
                fields.storedField(IndexedBankListFileHashFieldName, bankListFileHash)
            )

            writer.optimizeIndex()
        }
    }

    protected open fun createDocumentForBank(bank: BankInfo, writer: DocumentsWriter): Document {
        return writer.createDocumentForNonNullFields(
            fields.fullTextSearchField(BankInfoNameFieldName, bank.name, true),
            fields.keywordField(BankInfoBankCodeFieldName, bank.bankCode, true),
            fields.fullTextSearchField(BankInfoCityIndexedFieldName, bank.city, true),

            fields.storedField(BankInfoCityStoredFieldName, bank.city),
            fields.storedField(BankInfoBicFieldName, bank.bic),
            fields.storedField(BankInfoPostalCodeFieldName, bank.postalCode),
            fields.storedField(BankInfoChecksumMethodFieldName, bank.checksumMethod),
            fields.nullableStoredField(BankInfoPinTanServerAddressFieldName, bank.pinTanAddress),
            fields.nullableStoredField(BankInfoPinTanVersionFieldName, bank.pinTanVersion),
            fields.nullableStoredField(BankInfoOldBankCodeFieldName, bank.oldBankCode)
        )
    }


    protected open fun calculateCurrentBankListFileHash(): String {
        return calculateHash(readBankListFile())
    }

    protected open fun calculateHash(stringToHash: String): String {
        return hashService.hashString(HashAlgorithm.SHA512, stringToHash)
    }

}
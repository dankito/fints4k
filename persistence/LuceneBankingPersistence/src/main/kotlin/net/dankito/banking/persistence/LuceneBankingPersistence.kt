package net.dankito.banking.persistence

import net.dankito.utils.multiplatform.File
import net.dankito.banking.LuceneConfig
import net.dankito.banking.LuceneConfig.Companion.AmountFieldName
import net.dankito.banking.LuceneConfig.Companion.BalanceFieldName
import net.dankito.banking.LuceneConfig.Companion.BankAccountIdFieldName
import net.dankito.banking.LuceneConfig.Companion.BookingDateFieldName
import net.dankito.banking.LuceneConfig.Companion.DateSortFieldName
import net.dankito.banking.LuceneConfig.Companion.BookingTextFieldName
import net.dankito.banking.LuceneConfig.Companion.CurrencyFieldName
import net.dankito.banking.LuceneConfig.Companion.IdFieldName
import net.dankito.banking.LuceneConfig.Companion.OtherPartyAccountIdFieldName
import net.dankito.banking.LuceneConfig.Companion.OtherPartyBankCodeFieldName
import net.dankito.banking.LuceneConfig.Companion.OtherPartyNameFieldName
import net.dankito.banking.LuceneConfig.Companion.UsageFieldName
import net.dankito.banking.ui.model.*
import net.dankito.banking.util.ISerializer
import net.dankito.banking.util.JacksonJsonSerializer
import net.dankito.utils.lucene.index.DocumentsWriter
import net.dankito.utils.lucene.index.FieldBuilder
import org.apache.lucene.index.IndexableField
import org.slf4j.LoggerFactory


open class LuceneBankingPersistence(
    protected val indexFolder: File,
    databaseFolder: File,
    serializer: ISerializer = JacksonJsonSerializer()
) : BankingPersistenceJson(File(databaseFolder, "accounts.json"), serializer), IBankingPersistence {

    companion object {

        // i really hate this solution, but could find no other way to avoid app crashes when
        // Android app gets restored as previous IndexWriter is not not destroyed yet and holds
        // write lock and a new IndexWriter instance in DocumentsWriter gets instantiated
        protected var documentsWriter: DocumentsWriter? = null


        private val log = LoggerFactory.getLogger(LuceneBankingPersistence::class.java)

    }


    protected val fields = FieldBuilder()


    override fun saveOrUpdateAccountTransactions(account: TypedBankAccount, transactions: List<IAccountTransaction>) {
        val writer = getWriter()

        transactions.forEach { transaction ->
            writer.updateDocumentForNonNullFields(
                IdFieldName, transaction.technicalId,
                *createFieldsForAccountTransaction(account, transaction).toTypedArray()
            )
        }

        writer.flushChangesToDisk()
    }

    protected open fun createFieldsForAccountTransaction(account: TypedBankAccount, transaction: IAccountTransaction): List<IndexableField?> {
        return listOf(
            fields.keywordField(BankAccountIdFieldName, account.technicalId),
            fields.nullableFullTextSearchField(OtherPartyNameFieldName, transaction.otherPartyName, true),
            fields.fullTextSearchField(UsageFieldName, transaction.usage, true),
            fields.nullableFullTextSearchField(BookingTextFieldName, transaction.bookingText, true),

            fields.nullableStoredField(OtherPartyBankCodeFieldName, transaction.otherPartyBankCode),
            fields.nullableStoredField(OtherPartyAccountIdFieldName, transaction.otherPartyAccountId),
            fields.storedField(BookingDateFieldName, transaction.bookingDate),
            fields.storedField(AmountFieldName, transaction.amount),
            fields.storedField(CurrencyFieldName, transaction.currency),
            fields.nullableStoredField(BalanceFieldName, transaction.closingBalance), // TODO: remove

            fields.sortField(DateSortFieldName, transaction.valueDate)
        )
    }


    override fun deleteBank(bank: TypedBankData, allBanks: List<TypedBankData>) {
        try {
            deleteAccountTransactions(bank.accounts)
        } catch (e: Exception) {
            log.error("Could not delete account transactions of account $bank", e)
        }

        super.deleteBank(bank, allBanks)
    }

    protected open fun deleteAccountTransactions(accounts: List<TypedBankAccount>) {
        val writer = getWriter()

        val accountIds = accounts.map { it.technicalId }
        writer.deleteDocumentsAndFlushChangesToDisk(BankAccountIdFieldName, *accountIds.toTypedArray())
    }


    @Synchronized
    protected open fun getWriter(): DocumentsWriter {
        documentsWriter?.let { return it }

        val writer = DocumentsWriter(LuceneConfig.getAccountTransactionsIndexFolder(indexFolder))

        documentsWriter = writer

        return writer
    }

}
package net.dankito.banking.persistence

import net.dankito.banking.LuceneConfig
import net.dankito.banking.LuceneConfig.Companion.AmountFieldName
import net.dankito.banking.LuceneConfig.Companion.BankAccountIdFieldName
import net.dankito.banking.LuceneConfig.Companion.BookingDateFieldName
import net.dankito.banking.LuceneConfig.Companion.DateSortFieldName
import net.dankito.banking.LuceneConfig.Companion.BookingTextFieldName
import net.dankito.banking.LuceneConfig.Companion.IdFieldName
import net.dankito.banking.LuceneConfig.Companion.OtherPartyAccountIdFieldName
import net.dankito.banking.LuceneConfig.Companion.OtherPartyNameFieldName
import net.dankito.banking.LuceneConfig.Companion.UsageFieldName
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import net.dankito.utils.lucene.index.DocumentsWriter
import net.dankito.utils.lucene.index.FieldBuilder
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.serialization.JacksonJsonSerializer
import org.apache.lucene.index.IndexableField
import org.slf4j.LoggerFactory
import java.io.File


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


    override fun saveOrUpdateAccountTransactions(bankAccount: BankAccount, transactions: List<AccountTransaction>) {
        val writer = getWriter()

        transactions.forEach { transaction ->
            writer.updateDocumentForObject(transaction,
                IdFieldName, transaction.id,
                createFieldsForAccountTransaction(bankAccount, transaction)
            )
        }

        writer.flushChangesToDisk()
    }

    protected open fun createFieldsForAccountTransaction(bankAccount: BankAccount, transaction: AccountTransaction): List<IndexableField?> {
        return listOf(
            fields.keywordField(BankAccountIdFieldName, bankAccount.id),
            fields.nullableFullTextSearchField(OtherPartyNameFieldName, transaction.otherPartyName),
            fields.nullableStringField(OtherPartyAccountIdFieldName, transaction.otherPartyAccountId),
            fields.fullTextSearchField(UsageFieldName, transaction.usage),
            fields.nullableFullTextSearchField(BookingTextFieldName, transaction.bookingText),

            fields.dateField(BookingDateFieldName, transaction.bookingDate),
            fields.bigDecimalField(AmountFieldName, transaction.amount),

            fields.sortField(DateSortFieldName, transaction.valueDate)
        )
    }


    override fun deleteAccount(account: Account, allAccounts: List<Account>) {
        try {
            deleteAccountTransactions(account.bankAccounts)
        } catch (e: Exception) {
            log.error("Could not delete account transactions of account $account", e)
        }

        super.deleteAccount(account, allAccounts)
    }

    protected open fun deleteAccountTransactions(bankAccounts: List<BankAccount>) {
        val writer = getWriter()

        val bankAccountIds = bankAccounts.map { it.id }
        writer.deleteDocumentsAndFlushChangesToDisk(BankAccountIdFieldName, *bankAccountIds.toTypedArray())
    }


    @Synchronized
    protected open fun getWriter(): DocumentsWriter {
        documentsWriter?.let { return it }

        val writer = DocumentsWriter(LuceneConfig.getAccountTransactionsIndexFolder(indexFolder))

        documentsWriter = writer

        return writer
    }

}
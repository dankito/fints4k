package net.dankito.banking.persistence

import net.dankito.banking.LuceneConfig
import net.dankito.banking.LuceneConfig.Companion.AmountFieldName
import net.dankito.banking.LuceneConfig.Companion.BalanceFieldName
import net.dankito.banking.LuceneConfig.Companion.BankAccountIdFieldName
import net.dankito.banking.LuceneConfig.Companion.BookingDateFieldName
import net.dankito.banking.LuceneConfig.Companion.BookingDateSortFieldName
import net.dankito.banking.LuceneConfig.Companion.BookingTextFieldName
import net.dankito.banking.LuceneConfig.Companion.CurrencyFieldName
import net.dankito.banking.LuceneConfig.Companion.OtherPartyAccountIdFieldName
import net.dankito.banking.LuceneConfig.Companion.OtherPartyBankCodeFieldName
import net.dankito.banking.LuceneConfig.Companion.OtherPartyNameFieldName
import net.dankito.banking.LuceneConfig.Companion.UsageFieldName
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import net.dankito.utils.lucene.index.DocumentsWriter
import net.dankito.utils.lucene.index.FieldBuilder
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.serialization.JacksonJsonSerializer
import org.apache.lucene.index.IndexableField
import java.io.File
import java.util.concurrent.atomic.AtomicInteger


open class LuceneBankingPersistence(
    protected val indexFolder: File,
    databaseFolder: File,
    serializer: ISerializer = JacksonJsonSerializer()
) : BankingPersistenceJson(File(databaseFolder, "accounts.json"), serializer), IBankingPersistence {


    protected val fields = FieldBuilder()

    protected var documentsWriter: DocumentsWriter? = null

    protected val countWriterUsages = AtomicInteger(0)


    override fun saveOrUpdateAccountTransactions(bankAccount: BankAccount, transactions: List<AccountTransaction>) {
        val writer = getWriter()

        transactions.forEach { transaction ->
            writer.updateDocumentForNonNullFields(
                LuceneConfig.IdFieldName, transaction.id,
                *createFieldsForAccountTransaction(bankAccount, transaction).toTypedArray()
            )
        }

        writer.flushChangesToDisk()

        releaseWriter()
    }

    protected open fun createFieldsForAccountTransaction(bankAccount: BankAccount, transaction: AccountTransaction): List<IndexableField?> {
        return listOf(
            fields.keywordField(BankAccountIdFieldName, bankAccount.id),
            fields.nullableFullTextSearchField(OtherPartyNameFieldName, transaction.otherPartyName, true),
            fields.fullTextSearchField(UsageFieldName, transaction.usage, true),
            fields.nullableFullTextSearchField(BookingTextFieldName, transaction.bookingText, true),

            fields.nullableStoredField(OtherPartyBankCodeFieldName, transaction.otherPartyBankCode),
            fields.nullableStoredField(OtherPartyAccountIdFieldName, transaction.otherPartyAccountId),
            fields.storedField(BookingDateFieldName, transaction.bookingDate),
            fields.storedField(AmountFieldName, transaction.amount),
            fields.storedField(CurrencyFieldName, transaction.currency),
            fields.nullableStoredField(BalanceFieldName, transaction.balance),

            fields.sortField(BookingDateSortFieldName, transaction.bookingDate)
        )
    }


    @Synchronized
    protected open fun getWriter(): DocumentsWriter {
        countWriterUsages.incrementAndGet()

        documentsWriter?.let { return it }

        documentsWriter = DocumentsWriter(LuceneConfig.getAccountTransactionsIndexFolder(indexFolder))

        return documentsWriter!!
    }

    @Synchronized
    protected open fun releaseWriter() {
        val countUsages = countWriterUsages.decrementAndGet()

        if (countUsages == 0) {
            documentsWriter?.close()
        }
    }

}
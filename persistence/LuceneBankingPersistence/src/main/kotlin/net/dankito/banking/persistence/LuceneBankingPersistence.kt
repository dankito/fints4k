package net.dankito.banking.persistence

import net.dankito.banking.LuceneConfig
import net.dankito.banking.LuceneConfig.Companion.AmountFieldName
import net.dankito.banking.LuceneConfig.Companion.BalanceFieldName
import net.dankito.banking.LuceneConfig.Companion.BankAccountIdFieldName
import net.dankito.banking.LuceneConfig.Companion.BookingDateFieldName
import net.dankito.banking.LuceneConfig.Companion.BookingDateSortFieldName
import net.dankito.banking.LuceneConfig.Companion.BookingTextFieldName
import net.dankito.banking.LuceneConfig.Companion.CurrencyFieldName
import net.dankito.banking.LuceneConfig.Companion.IdFieldName
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
import java.io.File


open class LuceneBankingPersistence(
    protected val indexFolder: File,
    databaseFolder: File,
    serializer: ISerializer = JacksonJsonSerializer()
) : BankingPersistenceJson(File(databaseFolder, "accounts.json"), serializer), IBankingPersistence {


    protected val fields = FieldBuilder()


    override fun saveOrUpdateAccountTransactions(bankAccount: BankAccount, transactions: List<AccountTransaction>) {
        DocumentsWriter(LuceneConfig.getAccountTransactionsIndexFolder(indexFolder)).use { writer ->
            transactions.forEach { transaction ->
                writer.updateDocumentForNonNullFields(IdFieldName, transaction.id,
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

            writer.flushChangesToDisk()
        }
    }

}
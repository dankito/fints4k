package net.dankito.banking

import java.io.File


class LuceneConfig {

    companion object {
        const val BankAccountIdFieldName = "bank_account_id"

        const val IdFieldName = "id"

        const val OtherPartyNameFieldName = "other_party_name"

        const val OtherPartyAccountIdFieldName = "other_party_account_id"

        const val BookingDateFieldName = "booking_date"
        const val DateSortFieldName = "value_date_sort"

        const val UsageFieldName = "usage"

        const val BookingTextFieldName = "booking_text"

        const val AmountFieldName = "amount"


        fun getAccountTransactionsIndexFolder(indexFolder: File): File {
            return File(indexFolder, "account_transactions")
        }

    }

}
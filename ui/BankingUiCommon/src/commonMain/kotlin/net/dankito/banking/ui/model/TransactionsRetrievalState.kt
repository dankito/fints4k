package net.dankito.banking.ui.model


enum class TransactionsRetrievalState {

    AccountDoesNotSupportFetchingTransactions,

    NeverRetrievedTransactions,

    NoTransactionsInRetrievedPeriod,

    RetrievedTransactions

}
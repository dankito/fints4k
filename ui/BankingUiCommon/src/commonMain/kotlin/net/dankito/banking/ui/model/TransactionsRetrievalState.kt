package net.dankito.banking.ui.model


enum class TransactionsRetrievalState {

    AccountTypeNotSupported,

    AccountDoesNotSupportFetchingTransactions,

    NeverRetrievedTransactions,

    NoTransactionsInRetrievedPeriod,

    RetrievedTransactions

}
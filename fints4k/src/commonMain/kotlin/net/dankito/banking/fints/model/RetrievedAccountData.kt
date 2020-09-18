package net.dankito.banking.fints.model


open class RetrievedAccountData(
    val accountData: AccountData,
    val balance: Money?,
    var bookedTransactions: Collection<AccountTransaction>,
    var unbookedTransactions: List<Any>
) {

    open fun addBookedTransactions(transactions: List<AccountTransaction>) {
        val bookedTransactions = this.bookedTransactions.toMutableList() // some banks like Postbank return some transactions multiple times -> remove these

        bookedTransactions.addAll(transactions)

        this.bookedTransactions = bookedTransactions
    }

}
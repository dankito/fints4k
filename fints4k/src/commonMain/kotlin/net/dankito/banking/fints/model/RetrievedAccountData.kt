package net.dankito.banking.fints.model


open class RetrievedAccountData(
    open val accountData: AccountData,
    open val balance: Money?,
    open var bookedTransactions: Collection<AccountTransaction>,
    open var unbookedTransactions: List<Any>
) {

    open fun addBookedTransactions(transactions: List<AccountTransaction>) {
        val bookedTransactions = this.bookedTransactions.toMutableList() // some banks like Postbank return some transactions multiple times -> remove these

        bookedTransactions.addAll(transactions)

        this.bookedTransactions = bookedTransactions
    }

}
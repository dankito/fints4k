package net.dankito.banking.fints.model


open class RetrievedAccountData(
    open val accountData: AccountData,
    open val successfullyRetrievedData: Boolean,
    open val balance: Money?,
    open var bookedTransactions: Collection<AccountTransaction>,
    open var unbookedTransactions: Collection<Any>
) {

    companion object {

        fun unsuccessful(account: AccountData): RetrievedAccountData {
            return RetrievedAccountData(account, false, null, listOf(), listOf())
        }

        fun unsuccessfulList(account: AccountData): List<RetrievedAccountData> {
            return listOf(unsuccessful(account))
        }

    }
}
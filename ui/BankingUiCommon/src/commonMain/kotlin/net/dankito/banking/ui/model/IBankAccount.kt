package net.dankito.banking.ui.model

import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date


typealias TypedBankAccount = IBankAccount<IAccountTransaction>


interface IBankAccount<TTransaction: IAccountTransaction> : OrderedDisplayable {
    val bank: IBankData<*, *>
    var identifier: String
    var accountHolderName: String
    var iban: String?
    var subAccountNumber: String?
    var balance: BigDecimal
    var currency: String
    var type: BankAccountType
    var productName: String?
    var accountLimit: String?
    var retrievedTransactionsFromOn: Date?
    var retrievedTransactionsUpTo: Date?
    var supportsRetrievingAccountTransactions: Boolean
    var supportsRetrievingBalance: Boolean
    var supportsTransferringMoney: Boolean
    var supportsRealTimeTransfer: Boolean
    var bookedTransactions: List<TTransaction>
    var unbookedTransactions: List<Any>
    var technicalId: String
    var haveAllTransactionsBeenRetrieved: Boolean
    var isAccountTypeSupportedByApplication: Boolean
    var countDaysForWhichTransactionsAreKept: Int?
    var userSetDisplayName: String?

    /**
     * Account will not be visible in UI
     */
    var hideAccount: Boolean

    /**
     * Account is still visible in UI but will not be included in automatic accounts refresh (Kontorundruf) or if multiple accounts get updated.
     *
     * However it still can be updated if navigated to that single account and call update there.
     */
    var includeInAutomaticAccountsUpdate: Boolean

    /**
     * If there are still older transactions to fetch, that is [haveAllTransactionsBeenRetrieved] is [false], at a striking place,
     * e.g. above transactions list or with an overlay, an information will be displayed to fetch all transactions.
     *
     * However this information can be dismissed by user. Than it still will be visible below transactions list where it's not that well visible to user.
     */
    var doNotShowStrikingFetchAllTransactionsView: Boolean


    override val displayName: String
        get() {
            return userSetDisplayName ?: productName ?: subAccountNumber ?: identifier
        }


    fun addBookedTransactions(retrievedBookedTransactions: List<TTransaction>) {
        val uniqueTransactions = this.bookedTransactions.toMutableSet()

        uniqueTransactions.addAll(retrievedBookedTransactions)

        this.bookedTransactions = uniqueTransactions.toList()
    }

    fun addUnbookedTransactions(retrievedUnbookedTransactions: List<Any>) {
        val uniqueUnbookedTransactions = this.unbookedTransactions.toMutableSet()

        uniqueUnbookedTransactions.addAll(retrievedUnbookedTransactions)

        this.unbookedTransactions = uniqueUnbookedTransactions.toList()
    }


    val stringRepresentation: String
        get() = "$accountHolderName ($identifier)"
    
}
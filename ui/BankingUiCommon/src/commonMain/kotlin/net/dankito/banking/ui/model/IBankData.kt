package net.dankito.banking.ui.model

import net.dankito.banking.ui.model.tan.TanMedium
import net.dankito.banking.ui.model.tan.TanMediumStatus
import net.dankito.banking.ui.model.tan.TanMethod
import net.dankito.banking.util.sortedByDisplayIndex
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.sum


typealias TypedBankData = IBankData<IBankAccount<IAccountTransaction>, IAccountTransaction>


interface IBankData<TAccount: IBankAccount<TAccountTransaction>, TAccountTransaction: IAccountTransaction> : OrderedDisplayable {

    var bankCode: String
    var userName: String
    var password: String
    var finTsServerAddress: String

    var bankName: String
    var bic: String
    var customerName: String
    var userId: String

    var iconUrl: String?

    var accounts: List<TAccount>

    var supportedTanMethods: List<TanMethod>
    var selectedTanMethod: TanMethod?
    var tanMedia: List<TanMedium>

    var userSetDisplayName: String?

    var technicalId: String


    override val displayName: String
        get() = userSetDisplayName ?: bankName


    val accountsSorted: List<TAccount>
        get() = accounts.sortedByDisplayIndex()


    val balance: BigDecimal
        get() = accounts.map { it.balance }.sum()

    val transactions: List<IAccountTransaction>
        get() = accounts.flatMap { it.bookedTransactions }

    val tanMediaSorted: List<TanMedium>
        get() = tanMedia.sortedByDescending { it.status == TanMediumStatus.Used }


    var countDaysForWhichTransactionsAreKept: Int?


    val stringRepresentation: String
        get() = "$bankName $userName"

}
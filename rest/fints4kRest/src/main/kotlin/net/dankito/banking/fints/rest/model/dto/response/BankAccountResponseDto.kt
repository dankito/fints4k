package net.dankito.banking.fints.rest.model.dto.response

import net.dankito.banking.fints.response.segments.AccountType
import net.dankito.utils.multiplatform.Date
import java.math.BigDecimal


open class BankAccountResponseDto(
    open val accountIdentifier: String,
    open val subAccountAttribute: String?,
    open val iban: String?,
    open val accountType: AccountType?,
    open val currency: String?,
    open val accountHolderName: String,
    open val productName: String?,
    open val supportsRetrievingBalance: Boolean,
    open val supportsRetrievingAccountTransactions: Boolean,
    open val supportsTransferringMoney: Boolean,
    open val supportsInstantPaymentMoneyTransfer: Boolean,
    open val successfullyRetrievedData: Boolean,
    open val balance: BigDecimal?,
    open val retrievedTransactionsFrom: Date?,
    open val retrievedTransactionsTo: Date?,
    open var bookedTransactions: Collection<AccountTransactionResponseDto>,
    open var unbookedTransactions: Collection<Any>
)
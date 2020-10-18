package net.dankito.banking.fints.rest.model.dto.response

import java.math.BigDecimal


open class GetAccountTransactionsResponseDto(
    open val identifier: String,
    open val productName: String?,
    successful: Boolean,
    errorMessage: String?,
    open val balance: BigDecimal?,
    open var bookedTransactions: Collection<AccountTransactionResponseDto>,
    open var unbookedTransactions: Collection<Any>
) : ResponseDtoBase(successful, errorMessage)
package net.dankito.banking.fints.rest.model.dto.response

import net.dankito.utils.multiplatform.Date
import java.math.BigDecimal


open class AccountTransactionResponseDto(
    open val amount: BigDecimal,
    open val reference: String,
    open val otherPartyName: String?,
    open val otherPartyBankCode: String?,
    open val otherPartyAccountId: String?,
    open val bookingText: String?,
    open val valueDate: Date
)
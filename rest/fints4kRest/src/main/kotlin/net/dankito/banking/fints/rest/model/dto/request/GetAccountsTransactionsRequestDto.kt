package net.dankito.banking.fints.rest.model.dto.request

import net.dankito.banking.fints.rest.model.BankAccessData
import net.dankito.utils.multiplatform.Date


open class GetAccountsTransactionsRequestDto(
    open val credentials: BankAccessData,
    open val accounts: List<AccountRequestDto>,
    open val alsoRetrieveBalance: Boolean = true,
    open val fromDate: Date? = null,
    open val toDate: Date? = null,
    open val abortIfTanIsRequired: Boolean = false
)
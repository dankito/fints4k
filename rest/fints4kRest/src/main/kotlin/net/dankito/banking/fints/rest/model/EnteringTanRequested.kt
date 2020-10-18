package net.dankito.banking.fints.rest.model

import net.dankito.banking.fints.model.BankData
import net.dankito.banking.fints.model.TanChallenge


open class EnteringTanRequested(
    open val tanRequestId: String,
    open val bank: BankData,
    open val tanChallenge: TanChallenge
)
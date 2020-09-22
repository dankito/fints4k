package net.dankito.banking.model

import net.dankito.banking.ui.model.TypedBankData


open class AccountCredentials(
    var bankCode: String,
    var customerId: String,
    var password: String
) {

    constructor(bank: TypedBankData) : this(bank.bankCode, bank.customerId, bank.password)

}
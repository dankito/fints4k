package net.dankito.banking.model

import net.dankito.banking.ui.model.TypedCustomer


open class AccountCredentials(
    var bankCode: String,
    var customerId: String,
    var password: String
) {

    constructor(bank: TypedCustomer) : this(bank.bankCode, bank.customerId, bank.password)

}
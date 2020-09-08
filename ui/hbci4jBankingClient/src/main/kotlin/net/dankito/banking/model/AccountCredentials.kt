package net.dankito.banking.model

import net.dankito.banking.ui.model.Customer


open class AccountCredentials(
    var bankCode: String,
    var customerId: String,
    var password: String
) {

    constructor(bank: Customer) : this(bank.bankCode, bank.customerId, bank.password)

}
package net.dankito.banking.model


open class AccountCredentials(
    val bankCode: String,
    val customerId: String,
    var password: String
)
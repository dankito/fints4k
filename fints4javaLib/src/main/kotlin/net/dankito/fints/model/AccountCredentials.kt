package net.dankito.fints.model


open class AccountCredentials @JvmOverloads constructor(
    val bankCode: String,
    val customerId: String,
    val pin: String,
    val userId: String = customerId
)
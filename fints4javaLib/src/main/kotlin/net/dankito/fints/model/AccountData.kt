package net.dankito.fints.model

import net.dankito.fints.response.segments.AccountType


open class AccountData(
    val accountIdentifier: String,
    val subAccountAttribute: String?,
    val bankCountryCode: Int,
    val bankCode: String,
    val iban: String?,
    val customerId: String,
    val accountType: AccountType?,
    val currency: String?, // TODO: may parse to a value object
    val accountHolderName: String,
    val productName: String?,
    val accountLimit: String?,
    val allowedJobNames: List<String>
) {

    override fun toString(): String {
        return "$productName $accountIdentifier $accountHolderName"
    }

}
package net.dankito.fints.model

import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.fints.response.segments.AccountType
import net.dankito.fints.response.segments.JobParameters


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
    val allowedJobNames: List<String>,
    var allowedJobs: List<JobParameters> = listOf(),
    var supportsRetrievingAccountTransactions: Boolean = false,
    var supportsRetrievingBalance: Boolean = false,
    var supportsTransferringMoney: Boolean = false,
    var supportsRetrievingTransactionsOfLast90DaysWithoutTan: Boolean? = null,
    var triedToRetrieveTransactionsOfLast90DaysWithoutTan: Boolean = false
) {

    internal constructor() : this("", null, Laenderkennzeichen.Germany, "", null, "", null, null, "", null, null, listOf()) // for object deserializers

    override fun toString(): String {
        return "$productName $accountIdentifier $accountHolderName"
    }

}
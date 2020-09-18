package net.dankito.banking.fints.model

import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.banking.fints.response.segments.AccountType
import net.dankito.banking.fints.response.segments.JobParameters


open class AccountData(
    open val accountIdentifier: String,
    open val subAccountAttribute: String?,
    open val bankCountryCode: Int,
    open val bankCode: String,
    open val iban: String?,
    open val customerId: String,
    open val accountType: AccountType?,
    open val currency: String?, // TODO: may parse to a value object
    open val accountHolderName: String,
    open val productName: String?,
    open val accountLimit: String?,
    open val allowedJobNames: List<String>,
    open var allowedJobs: List<JobParameters> = listOf()
) {

    internal constructor() : this("", null, Laenderkennzeichen.Germany, "", null, "", null, null, "", null, null, listOf()) // for object deserializers


    protected open val supportedFeatures = mutableSetOf<AccountFeature>()


    open fun supportsFeature(feature: AccountFeature): Boolean {
        return supportedFeatures.contains(feature)
    }

    open fun setSupportsFeature(feature: AccountFeature, isSupported: Boolean) {
        if (isSupported) {
            supportedFeatures.add(feature)
        }
        else {
            supportedFeatures.remove(feature)
        }
    }


    override fun toString(): String {
        return "$productName $accountIdentifier $accountHolderName"
    }

}
package net.dankito.banking.fints.response.segments

import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung


open class KontoverbindungZvInternational(
    val isSepaAccount: Boolean,
    val iban: String?,
    val bic: String?,
    val accountIdentifier: String,
    val subAccountAttribute: String? = null,
    val bankInfo: Kreditinstitutskennung
)
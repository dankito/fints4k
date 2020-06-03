package net.dankito.banking.fints.messages.datenelementgruppen.implementierte.account

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.implementierte.account.BIC
import net.dankito.banking.fints.messages.datenelemente.implementierte.account.IBAN
import net.dankito.banking.fints.messages.datenelemente.implementierte.account.KontoDepotnummer
import net.dankito.banking.fints.messages.datenelemente.implementierte.account.Unterkontomerkmal
import net.dankito.banking.fints.messages.datenelementgruppen.Datenelementgruppe
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung
import net.dankito.banking.fints.model.AccountData
import net.dankito.banking.fints.model.BankData


/**
 * Die Kontoverbindung international dient zur Verwendung von IBAN und BIC sowie auch der
 * nationalen Elemente Kreditinstitutskennung und Konto-/Depotnummer mit optionalem
 * Unterkontomerkmal, strukturell angelehnt an das Aggregate „Account“ in ISO20022.
 */
open class KontoverbindungInternational(
    iban: String?,
    bic: String?,
    bankCountryCode: Int? = null,
    bankCode: String? = null,
    accountNumber: String? = null,
    subAccountAttribute: String? = null

) : Datenelementgruppe(listOf(
    IBAN(iban ?: "", Existenzstatus.Optional),
    BIC(bic ?: "", Existenzstatus.Optional),
    KontoDepotnummer(accountNumber, Existenzstatus.Optional),
    Unterkontomerkmal(subAccountAttribute, Existenzstatus.Optional),
    Kreditinstitutskennung(bankCountryCode ?: 0, bankCode ?: "", if (bankCountryCode != null && bankCode != null) Existenzstatus.Optional else Existenzstatus.NotAllowed)
), Existenzstatus.Mandatory) {

    constructor(account: AccountData, bank: BankData) : this(account, bank.bic)

    constructor(account: AccountData, bic: String)
            : this(account.iban, bic, account.bankCountryCode, account.bankCode, account.accountIdentifier, account.subAccountAttribute)
}
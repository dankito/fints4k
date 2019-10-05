package net.dankito.fints.model

import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.KundenID
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemID
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatusWerte
import net.dankito.fints.messages.datenelemente.implementierte.signatur.IdentifizierungDerPartei
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsverfahren
import net.dankito.fints.messages.datenelemente.implementierte.signatur.VersionDesSicherheitsverfahrens


open class CustomerData(
    val customerId: String,
    var pin: String,
    val userId: String = customerId,
    var updVersion: Int = 0,
    var availableTanProcedures: List<TanProcedure> = listOf(),
    var selectedTanProcedure: TanProcedure? = null,
    var securityMethod: Sicherheitsverfahren = Sicherheitsverfahren.PIN_TAN_Verfahren,
    var version: VersionDesSicherheitsverfahrens = VersionDesSicherheitsverfahrens.PIN_Zwei_Schritt,
    var selectedLanguage: Dialogsprache = Dialogsprache.Default,
    var customerSystemId: String = KundensystemID.Anonymous,
    var customerSystemStatus: KundensystemStatusWerte = KundensystemStatusWerte.Benoetigt,
    var partyIdentification: String = IdentifizierungDerPartei.SynchronizingCustomerSystemId
) {

    companion object {
        val Anonymous = CustomerData(KundenID.Anonymous, "", customerSystemStatus = KundensystemStatusWerte.NichtBenoetigt)
    }

    override fun toString(): String {
        return customerId
    }

}
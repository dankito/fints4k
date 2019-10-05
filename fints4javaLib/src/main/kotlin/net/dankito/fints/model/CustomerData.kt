package net.dankito.fints.model

import net.dankito.fints.messages.datenelemente.implementierte.*
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsverfahren
import net.dankito.fints.messages.datenelemente.implementierte.signatur.VersionDesSicherheitsverfahrens


open class CustomerData(
    val customerId: String,
    var pin: String,
    val userId: String = customerId,
    var updVersion: Int = UPDVersion.VersionNotReceivedYet,
    var availableTanProcedures: List<TanProcedure> = listOf(),
    var selectedTanProcedure: TanProcedure? = null,
    var securityMethod: Sicherheitsverfahren = Sicherheitsverfahren.PIN_TAN_Verfahren,
    var version: VersionDesSicherheitsverfahrens = VersionDesSicherheitsverfahrens.PIN_Zwei_Schritt,
    var selectedLanguage: Dialogsprache = Dialogsprache.Default,
    var customerSystemId: String = KundensystemID.Anonymous,
    var customerSystemStatus: KundensystemStatusWerte = KundensystemStatus.SynchronizingCustomerSystemId
) {

    companion object {
        val Anonymous = CustomerData(KundenID.Anonymous, "", customerSystemStatus = KundensystemStatusWerte.NichtBenoetigt)
    }

    override fun toString(): String {
        return customerId
    }

}
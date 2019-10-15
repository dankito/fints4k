package net.dankito.fints.model

import net.dankito.fints.messages.datenelemente.implementierte.*
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion


open class CustomerData(
    val customerId: String,
    var pin: String,
    val userId: String = customerId,
    var name: String = "",
    var iban: String? = null,
    var accounts: List<AccountData> = listOf(),
    var updVersion: Int = UPDVersion.VersionNotReceivedYet,
    var supportedTanProcedures: List<TanProcedure> = listOf(),
    var selectedTanProcedure: TanProcedure = TanProcedureNotSelected,
    var selectedLanguage: Dialogsprache = Dialogsprache.Default,
    var customerSystemId: String = KundensystemID.Anonymous,
    var customerSystemStatus: KundensystemStatusWerte = KundensystemStatus.SynchronizingCustomerSystemId,
    var supportsRetrievingTransactionsOfLast90DaysWithoutTan: Boolean? = null,
    var triedToRetrieveTransactionsOfLast90DaysWithoutTan: Boolean = false
) {

    companion object {
        val SecurityFunctionNotSelected = Sicherheitsfunktion.Einschritt_Verfahren

        val TanProcedureNotSelected = TanProcedure("NOT_SELECTED", SecurityFunctionNotSelected, TanProcedureType.EnterTan)

        val Anonymous = CustomerData(KundenID.Anonymous, "", customerSystemStatus = KundensystemStatusWerte.NichtBenoetigt)
    }


    // for Java
    constructor(customerId: String, pin: String) : this(customerId, pin, customerId)


    val isTanProcedureSelected: Boolean
        get() = selectedTanProcedure != TanProcedureNotSelected


    open fun resetSelectedTanProcedure() {
        selectedTanProcedure = TanProcedureNotSelected
    }

    open fun resetUpdVersion() {
        updVersion = UPDVersion.VersionNotReceivedYet
    }


    override fun toString(): String {
        return customerId
    }

}
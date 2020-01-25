package net.dankito.fints.model

import net.dankito.fints.messages.datenelemente.implementierte.*
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedium


open class CustomerData(
    val customerId: String,
    var pin: String,
    val userId: String = customerId,
    var name: String = "",
    val accounts: List<AccountData> = mutableListOf(),
    var updVersion: Int = UPDVersion.VersionNotReceivedYet,
    var supportedTanProcedures: List<TanProcedure> = listOf(),
    var selectedTanProcedure: TanProcedure = TanProcedureNotSelected,
    var tanMedia: List<TanMedium> = listOf(),
    var selectedLanguage: Dialogsprache = Dialogsprache.Default,
    var customerSystemId: String = KundensystemID.Anonymous,
    var customerSystemStatus: KundensystemStatusWerte = KundensystemStatus.SynchronizingCustomerSystemId
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


    open fun addAccount(account: AccountData) {
        (accounts as? MutableList)?.add(account)
    }


    override fun toString(): String {
        return customerId
    }

}
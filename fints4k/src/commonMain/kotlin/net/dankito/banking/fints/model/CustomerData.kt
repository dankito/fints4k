package net.dankito.banking.fints.model

import net.dankito.banking.fints.messages.datenelemente.implementierte.*
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMedium


open class CustomerData(
    var customerId: String,
    var pin: String,
    var userId: String = customerId,
    var name: String = "",
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

    internal constructor() : this("", "") // for object deserializers


    protected val accountsField = mutableListOf<AccountData>()

    open val accounts: List<AccountData>
        get() = ArrayList(accountsField)


    open val isTanProcedureSelected: Boolean
        get() = selectedTanProcedure != TanProcedureNotSelected


    open fun resetSelectedTanProcedure() {
        selectedTanProcedure = TanProcedureNotSelected
    }

    open fun resetUpdVersion() {
        updVersion = UPDVersion.VersionNotReceivedYet
    }


    open fun addAccount(account: AccountData) {
        accountsField.add(account)
    }

    open fun removeAccount(account: AccountData) {
        accountsField.remove(account)
    }


    override fun toString(): String {
        return customerId
    }

}
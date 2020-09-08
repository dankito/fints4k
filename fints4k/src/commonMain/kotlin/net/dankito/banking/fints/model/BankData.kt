package net.dankito.banking.fints.model

import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.banking.fints.messages.datenelemente.implementierte.*
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMedium
import net.dankito.banking.fints.response.segments.ChangeTanMediaParameters
import net.dankito.banking.fints.response.segments.JobParameters
import net.dankito.banking.fints.response.segments.PinInfo


open class BankData(
    var bankCode: String,
    var customerId: String,
    var pin: String,
    var finTs3ServerAddress: String,
    var bic: String,

    var bankName: String = "",
    var countryCode: Int = Laenderkennzeichen.Germany, // TODO: currently there are only German banks. But change this if ever other countries get supported
    var bpdVersion: Int = BPDVersion.VersionNotReceivedYet,

    var userId: String = customerId,
    var customerName: String = "",
    var updVersion: Int = UPDVersion.VersionNotReceivedYet,

    var tanProceduresSupportedByBank: List<TanProcedure> = listOf(),
    var tanProceduresAvailableForUser: List<TanProcedure> = listOf(),
    var selectedTanProcedure: TanProcedure = TanProcedureNotSelected,
    var tanMedia: List<TanMedium> = listOf(),
    var changeTanMediumParameters: ChangeTanMediaParameters? = null,
    var pinInfo: PinInfo? = null,

    var supportedLanguages: List<Dialogsprache> = listOf(),
    var selectedLanguage: Dialogsprache = Dialogsprache.Default,
    var customerSystemId: String = KundensystemID.Anonymous,
    var customerSystemStatus: KundensystemStatusWerte = KundensystemStatus.SynchronizingCustomerSystemId,

    /**
     * Maximale Anzahl an Geschäftsvorfallsarten, die pro Nachricht zulässig ist.
     * Der Wert ‚0’ gibt an, dass keine Restriktionen bzgl. der Anzahl an Geschäftsvorfallsarten bestehen.
     */
    var countMaxJobsPerMessage: Int = 0,

    var supportedHbciVersions: List<HbciVersion> = listOf(),
    var supportedJobs: List<JobParameters> = listOf()
) {

    companion object {
        val SecurityFunctionNotSelected = Sicherheitsfunktion.Einschritt_Verfahren

        val TanProcedureNotSelected = TanProcedure("NOT_SELECTED", SecurityFunctionNotSelected, TanProcedureType.EnterTan)

        // TODO: is the BIC really needed at anonymous dialog init?
        fun anonymous(bankCode: String, finTs3ServerAddress: String, bic: String): BankData {
            return BankData(bankCode, KundenID.Anonymous, "", finTs3ServerAddress, bic, customerSystemStatus = KundensystemStatusWerte.NichtBenoetigt)
        }
    }


    internal constructor() : this("", "", "", "", "") // for object deserializers


    init {
        // for UniCredit / HypVereinsbank for online banking '70020270' has to be used as bank code
        if (bankName.contains("unicredit", true)) {
            bankCode = "70020270"
        }
    }


    protected val _accounts = mutableListOf<AccountData>()

    open val accounts: List<AccountData>
        get() = ArrayList(_accounts)


    open val isTanProcedureSelected: Boolean
        get() = selectedTanProcedure != TanProcedureNotSelected


    open fun addAccount(account: AccountData) {
        _accounts.add(account)
    }

    open fun removeAccount(account: AccountData) {
        _accounts.remove(account)
    }


    open fun resetBpdVersion() {
        bpdVersion = BPDVersion.VersionNotReceivedYet
    }

    open fun resetUpdVersion() {
        updVersion = UPDVersion.VersionNotReceivedYet
    }

    open fun resetSelectedTanProcedure() {
        selectedTanProcedure = TanProcedureNotSelected
    }


    override fun toString(): String {
        return "$bankCode $customerId"
    }

}
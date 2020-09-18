package net.dankito.banking.fints.model

import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.banking.fints.messages.datenelemente.implementierte.*
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMedium
import net.dankito.banking.fints.response.segments.ChangeTanMediaParameters
import net.dankito.banking.fints.response.segments.JobParameters
import net.dankito.banking.fints.response.segments.PinInfo


open class BankData(
    open var bankCode: String,
    open var customerId: String,
    open var pin: String,
    open var finTs3ServerAddress: String,
    open var bic: String,

    open var bankName: String = "",
    open var countryCode: Int = Laenderkennzeichen.Germany, // TODO: currently there are only German banks. But change this if ever other countries get supported
    open var bpdVersion: Int = BPDVersion.VersionNotReceivedYet,

    open var userId: String = customerId,
    open var customerName: String = "",
    open var updVersion: Int = UPDVersion.VersionNotReceivedYet,

    open var tanProceduresSupportedByBank: List<TanProcedure> = listOf(),
    open var tanProceduresAvailableForUser: List<TanProcedure> = listOf(),
    open var selectedTanProcedure: TanProcedure = TanProcedureNotSelected,
    open var tanMedia: List<TanMedium> = listOf(),
    open var changeTanMediumParameters: ChangeTanMediaParameters? = null,
    open var pinInfo: PinInfo? = null,

    open var supportedLanguages: List<Dialogsprache> = listOf(),
    open var selectedLanguage: Dialogsprache = Dialogsprache.Default,
    open var customerSystemId: String = KundensystemID.Anonymous,
    open var customerSystemStatus: KundensystemStatusWerte = KundensystemStatus.SynchronizingCustomerSystemId,

    /**
     * Maximale Anzahl an Geschäftsvorfallsarten, die pro Nachricht zulässig ist.
     * Der Wert ‚0’ gibt an, dass keine Restriktionen bzgl. der Anzahl an Geschäftsvorfallsarten bestehen.
     */
    open var countMaxJobsPerMessage: Int = 0,

    open var supportedHbciVersions: List<HbciVersion> = listOf(),
    open var supportedJobs: List<JobParameters> = listOf()
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



    protected open val _accounts = mutableListOf<AccountData>()

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


    /**
     * Some banks use a special bank code for online banking that doesn't match bank's bank code, e. g. Hypo Vereinsbank
     */
    open val bankCodeForOnlineBanking: String
        get() {
            // for UniCredit / HypVereinsbank for online banking '70020270' has to be used as bank code
            if (bankName.contains("unicredit", true)) {
                return "70020270"
            }

            return bankCode
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
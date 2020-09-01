package net.dankito.banking.fints.model

import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.banking.fints.messages.datenelemente.implementierte.BPDVersion
import net.dankito.banking.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.banking.fints.messages.datenelemente.implementierte.HbciVersion
import net.dankito.banking.fints.response.segments.ChangeTanMediaParameters
import net.dankito.banking.fints.response.segments.JobParameters
import net.dankito.banking.fints.response.segments.PinInfo


open class BankData(
    var bankCode: String,
    var finTs3ServerAddress: String,
    var bic: String,
    var name: String = "",
    var countryCode: Int = Laenderkennzeichen.Germany, // TODO: currently there are only German banks. But change this if ever other countries get supported
    var bpdVersion: Int = BPDVersion.VersionNotReceivedYet,

    /**
     * Maximale Anzahl an Geschäftsvorfallsarten, die pro Nachricht zulässig ist.
     * Der Wert ‚0’ gibt an, dass keine Restriktionen bzgl. der Anzahl an Geschäftsvorfallsarten bestehen.
     */
    var countMaxJobsPerMessage: Int = 0,

    var supportedHbciVersions: List<HbciVersion> = listOf(),
    var supportedTanProcedures: List<TanProcedure> = listOf(),
    var changeTanMediumParameters: ChangeTanMediaParameters? = null,
    var pinInfo: PinInfo? = null,
    var supportedLanguages: List<Dialogsprache> = listOf(),
    var supportedJobs: List<JobParameters> = listOf()
) {


    internal constructor() : this("", "", "") // for object deserializers


    init {
        // for UniCredit / HypVereinsbank for online banking '70020270' has to be used as bank code
        if (name.contains("unicredit", true)) {
            bankCode = "70020270"
        }
    }


    open fun resetBpdVersion() {
        bpdVersion = BPDVersion.VersionNotReceivedYet
    }


    override fun toString(): String {
        return "$name ($bankCode)"
    }

}
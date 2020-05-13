package net.dankito.fints.model

import net.dankito.fints.messages.datenelemente.implementierte.BPDVersion
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.HbciVersion
import net.dankito.fints.response.segments.ChangeTanMediaParameters
import net.dankito.fints.response.segments.JobParameters
import net.dankito.fints.response.segments.PinInfo


open class BankData(
    var bankCode: String,
    var countryCode: Int,
    var finTs3ServerAddress: String,
    var bic: String,
    var bpdVersion: Int = BPDVersion.VersionNotReceivedYet,
    var name: String = "",

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


    internal constructor() : this("", 0, "", "") // for object deserializers


    open fun resetBpdVersion() {
        bpdVersion = BPDVersion.VersionNotReceivedYet
    }


    override fun toString(): String {
        return "$name ($bankCode)"
    }

}
package net.dankito.banking.fints.response.segments

import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.*


open class TanProcedureParameters(
    val securityFunction: Sicherheitsfunktion,
    val tanProcess: TanProcess,
    val technicalTanProcedureIdentification: String,
    val zkaTanProcedure: ZkaTanProcedure?,
    val versionZkaTanProcedure: String?,
    val procedureName: String,
    val maxTanInputLength: Int,
    val allowedTanFormat: AllowedTanFormat,
    val descriptionToShowToUser: String,
    val maxReturnValueLength: Int,
    val multipleTansAllowed: Boolean,
    val timeAndDialogRelation: TanZeitUndDialogbezug,
    val cancellationAllowed: Boolean,
    val smsDebitAccountRequired: SmsAbbuchungskontoErforderlich,
    val initiatorAccountRequired: AuftraggeberkontoErforderlich,
    val challengeClassRequired: Boolean,
    val signatureStructured: Boolean,
    val initializingMode: Initialisierungsmodus,
    val nameOfTanMediaRequired: BezeichnungDesTanMediumsErforderlich,
    val hhdUcResponseRequired: Boolean,
    val countSupportedActiveTanMedia: Int?
) {

    override fun toString(): String {
        return "$procedureName $technicalTanProcedureIdentification"
    }

}
package net.dankito.banking.fints.response.segments

import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.*


open class TanMethodParameters(
    val securityFunction: Sicherheitsfunktion,
    val tanProcess: TanProcess,
    val technicalTanMethodIdentification: String,
    val zkaTanMethod: ZkaTanMethod?,
    val versionZkaTanMethod: String?,
    val methodName: String,
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


    internal constructor() : this(Sicherheitsfunktion.Klartext, TanProcess.TanProcess1, "", null, null, "", -1,
    AllowedTanFormat.Alphanumeric, "", -1, false, TanZeitUndDialogbezug.NotSupported, false, SmsAbbuchungskontoErforderlich.SmsAbbuchungskontoDarfNichtAngegebenWerden, AuftraggeberkontoErforderlich.AuftraggeberkontoDarfNichtAngegebenWerden,
    false, false, Initialisierungsmodus.InitialisierungsverfahrenMitKlartextPinOhneTan, BezeichnungDesTanMediumsErforderlich.BezeichnungDesTanMediumsDarfNichtAngegebenWerden, false, null) // for object deserializers


    override fun toString(): String {
        return "$methodName $technicalTanMethodIdentification"
    }

}
package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.tan.Auftragsreferenz
import net.dankito.fints.messages.datenelemente.implementierte.tan.TANProzessDatenelement
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanProcess
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.SegmentId


open class ZweiSchrittTanEinreichung(
    segmentNumber: Int,
    process: TanProcess,
    jobReference: String,
    challenge: String = "",
    challgendHHD_UC: ByteArray = byteArrayOf(),
    date: Int? = null,
    time: Int? = null,
    tanMediaIdentifier: String? = "N" // TODO: why 'N'?

) : Segment(listOf(
        Segmentkopf(SegmentId.Tan, 6, segmentNumber),
        TANProzessDatenelement(process),
    //    AuftragsHashwert(), // M: bei AuftragsHashwertverfahren<>0 und TAN-Prozess=1. N: sonst
        Auftragsreferenz(jobReference, Existenzstatus.Mandatory) // M: bei TAN-Prozess=2, 3, 4. O: bei TAN-Prozess=1
    //    ChallengeVersion3(challenge, Existenzstatus.Mandatory), // M: bei TAN-Prozess=1, 3, 4. O: bei TAN-Prozess=2
    //    ChallengeHHD_UC(challgendHHD_UC, Existenzstatus.Optional),
    //    NotAllowedDatenelement(), // GueltigkeitsdatumUndUhrzeitFuerChallenge // TODO: how to not write an element if it's optional and its paramters (date and time) are not set?
    //    BezeichnungDesTANMediums(tanMediaIdentifier ?: "", Existenzstatus.Optional)// M: bei TAN-Prozess=1, 3, 4 und „Anzahl unterstützter aktiver TAN-Medien“ nicht vorhanden. O: sonst
), Existenzstatus.Mandatory)
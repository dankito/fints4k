package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.JaNein
import net.dankito.fints.messages.datenelemente.implementierte.NotAllowedDatenelement
import net.dankito.fints.messages.datenelemente.implementierte.Segmentkennung
import net.dankito.fints.messages.datenelemente.implementierte.tan.*
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.fints.messages.segmente.id.ISegmentId


open class ZweiSchrittTanEinreichung(
    segmentNumber: Int,
    process: TanProcess,
    segmentIdForWhichTanShouldGetGenerated: ISegmentId? = null,
    jobHashValue: String? = null,
    jobReference: String? = null,
    furtherTanFollows: Boolean? = false,
    cancelJob: Boolean? = false,
    tanMediaIdentifier: String? = null

) : Segment(listOf(
        Segmentkopf(CustomerSegmentId.Tan, 6, segmentNumber),
        TANProzessDatenelement(process),
        Segmentkennung(segmentIdForWhichTanShouldGetGenerated?.id ?: ""), // M: bei TAN-Prozess=1. M: bei TAN-Prozess=4 und starker Authentifizierung. N: sonst
        NotAllowedDatenelement(), // Kontoverbindung // M: bei TAN-Prozess=1 und "Auftraggeberkonto erforderlich"=2 und Kontoverbindung im Auftrag enthalten. N: sonst
        AuftragsHashwert(jobHashValue ?: "", Existenzstatus.NotAllowed), // M: bei AuftragsHashwertverfahren<>0 und TAN-Prozess=1. N: sonst
        Auftragsreferenz(jobReference ?: "", Existenzstatus.Mandatory), // M: bei TAN-Prozess=2, 3, 4. O: bei TAN-Prozess=1
        JaNein(furtherTanFollows, Existenzstatus.NotAllowed), // M: bei TAN-Prozess=1, 2. N: bei TAN-Prozess=3, 4
        JaNein(cancelJob, Existenzstatus.NotAllowed), // O: bei TAN-Prozess=2 und „Auftragsstorno erlaubt“=J. N: sonst
        NotAllowedDatenelement(), // TODO: SMS-Abbuchungskonto // M: Bei TAN-Process=1, 3, 4 und „SMS-Abbuchungskonto erforderlich“=2. O: sonst
        NotAllowedDatenelement(), // TODO: Challenge-Klasse // M: bei TAN-Prozess=1 und „Challenge-Klasse erforderlich“=J. N: sonst
        NotAllowedDatenelement(), // TODO: Parameter Challenge-Klasse // O: Bei TAN-Process=1 „Challenge-Klasse erforderlich“=J. N: sonst
        BezeichnungDesTANMediums(tanMediaIdentifier, Existenzstatus.Optional), // M: bei TAN-Prozess=1, 3, 4 und „Anzahl unterstützter aktiver TAN-Medien“ nicht vorhanden. O: sonst
        NotAllowedDatenelement() // TODO: Antwort HHD_UC // M: bei TAN-Prozess=2 und „Antwort HHD_UC erforderlich“=“J“. O: sonst
))
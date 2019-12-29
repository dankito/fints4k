package net.dankito.fints.messages.segmente.implementierte.tan

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.fints.messages.datenelemente.implementierte.allCodes
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedienArtVersion
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanMediumKlasseVersion
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.id.CustomerSegmentId


open class TanGeneratorListeAnzeigen(
    segmentVersion: Int,
    segmentNumber: Int,
    tanMediaKind: TanMedienArtVersion = TanMedienArtVersion.Alle,
    tanMediumClass: TanMediumKlasseVersion = TanMediumKlasseVersion.AlleMedien
)
    : Segment(listOf(
    Segmentkopf(CustomerSegmentId.TanMediaList, segmentVersion, segmentNumber),
    Code(tanMediaKind, allCodes<TanMedienArtVersion>(), Existenzstatus.Mandatory),
    Code(tanMediumClass, allCodes<TanMediumKlasseVersion>(), Existenzstatus.Mandatory)
)) {

    init {
        val supportedMediaClasses = TanMediumKlasseVersion.values().filter { it.supportedHkTabVersions.contains(segmentVersion) }

        if (supportedMediaClasses.contains(tanMediumClass) == false) {
            throw UnsupportedOperationException("Value $tanMediumClass for TAN medium class is not valid for HKTAB version $segmentVersion. " +
                    "Supported values are: " + TanMediumKlasseVersion.values().filter { it.supportedHkTabVersions.contains(segmentVersion) }.map { it.code })
        }
    }

}
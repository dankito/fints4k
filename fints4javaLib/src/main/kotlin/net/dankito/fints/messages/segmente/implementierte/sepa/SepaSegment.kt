package net.dankito.fints.messages.segmente.implementierte.sepa

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement
import net.dankito.fints.messages.datenelemente.implementierte.sepa.SepaMessage
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.datenelementgruppen.implementierte.account.KontoverbindungInternational
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.id.ISegmentId


open class SepaSegment(
    segmentNumber: Int,
    segmentId: ISegmentId,
    segmentVersion: Int,
    sepaDescriptorUrn: String,
    sepaFileName: String,
    iban: String,
    bic: String,
    replacementStrings: Map<String, String>,
    messageCreator: ISepaMessageCreator = SepaMessageCreator()
)
    : Segment(listOf(
        Segmentkopf(segmentId, segmentVersion, segmentNumber),
        KontoverbindungInternational(iban, bic, null),
        object : AlphanumerischesDatenelement(sepaDescriptorUrn, Existenzstatus.Mandatory, 256) { },
        SepaMessage(sepaFileName, replacementStrings, messageCreator)
))
package net.dankito.banking.fints.messages.segmente.implementierte.sepa

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement
import net.dankito.banking.fints.messages.datenelemente.implementierte.sepa.SepaMessage
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.account.KontoverbindungInternational
import net.dankito.banking.fints.messages.segmente.Segment
import net.dankito.banking.fints.messages.segmente.id.ISegmentId
import net.dankito.banking.fints.model.AccountData


open class SepaSegment(
    segmentNumber: Int,
    segmentId: ISegmentId,
    segmentVersion: Int,
    sepaDescriptorUrn: String,
    sepaFileName: String,
    account: AccountData,
    bic: String,
    replacementStrings: Map<String, String>,
    messageCreator: ISepaMessageCreator = SepaMessageCreator()
)
    : Segment(listOf(
        Segmentkopf(segmentId, segmentVersion, segmentNumber),
        KontoverbindungInternational(account, bic),
        object : AlphanumerischesDatenelement(sepaDescriptorUrn, Existenzstatus.Mandatory, 256) { },
        SepaMessage(sepaFileName, replacementStrings, messageCreator)
))
package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.encryption.PinTanVerschluesselteDatenDatenelement
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.SegmentId


/**
 * Dieses Segment enthält die verschlüsselten (und komprimierten) Daten.
 */
open class VerschluesselteDaten(
    payload: String

) : Segment(listOf(
    Segmentkopf(SegmentId.EncryptionData, 1, 999),
    PinTanVerschluesselteDatenDatenelement(payload)
), Existenzstatus.Mandatory)
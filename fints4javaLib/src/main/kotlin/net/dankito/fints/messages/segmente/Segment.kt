package net.dankito.fints.messages.segmente

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.Separators
import net.dankito.fints.messages.datenelemente.DatenelementBase


abstract class Segment(val dataElementsAndGroups: List<DatenelementBase>, existenzstatus: Existenzstatus)
    : DatenelementBase(existenzstatus) {

    override fun format(): String {
        return dataElementsAndGroups.joinToString(Separators.DataElementGroupsSeparator) { it.format() }
    }

}
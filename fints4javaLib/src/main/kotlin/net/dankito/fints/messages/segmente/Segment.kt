package net.dankito.fints.messages.segmente

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.DatenelementBase


abstract class Segment(val dataElementsAndGroups: List<DatenelementBase>, existenzstatus: Existenzstatus)
    : DatenelementBase(existenzstatus) {

    companion object {
        const val DataElementGroupsSeparator = "+"
    }


    override fun format(): String {
        // TODO: really use DatenelementGruppenSeparator for all elements or use for Datenelement Datenelementgruppe.DatenelementSeparator ?
        return dataElementsAndGroups.joinToString(DataElementGroupsSeparator) { it.format() }
    }

}
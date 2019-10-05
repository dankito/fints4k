package net.dankito.fints.messages.datenelementgruppen

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.Separators
import net.dankito.fints.messages.datenelemente.DatenelementBase


abstract class Datenelementgruppe(val dataElements: List<DatenelementBase>, existenzstatus: Existenzstatus)
    : DatenelementBase(existenzstatus) {

    override fun format(): String {
        return dataElements.joinToString(Separators.DataElementsSeparator) { it.format() }
    }

}
package net.dankito.banking.fints.messages.datenelementgruppen

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.Separators
import net.dankito.banking.fints.messages.datenelemente.DatenelementBase


abstract class Datenelementgruppe(val dataElements: List<DatenelementBase>, existenzstatus: Existenzstatus)
    : DatenelementBase(existenzstatus) {

    companion object {
        val ReplaceEmptyDataElementsSeparatorsAtEndPattern = Regex("${Separators.DataElementsSeparator}*\$")
    }


    override fun format(): String {
        val formattedDataElementGroup = dataElements.joinToString(Separators.DataElementsSeparator) { it.format() }

        return cutEmptyDataElementGroupsAtEndOfSegment(formattedDataElementGroup)
    }

    /**
     * Auslassen von Datenelementen durch Abschneiden
     * Ist für DE, die am Ende eines Segments stehen, kein Inhalt vorhanden, können sie ausgelassen werden.
     * In diesem Fall wird das Segmentende-Zeichen unmittelbar nach dem letzten mit Inhalt belegten DE angegeben. (...)
     *
     * Auslassen von Gruppendatenelementen
     * Es gelten analog die Ausführungen zur Auslassung von Datenelementen.
     */
    protected open fun cutEmptyDataElementGroupsAtEndOfSegment(formattedDataElementGroup: String): String {
        return ReplaceEmptyDataElementsSeparatorsAtEndPattern.replaceFirst(formattedDataElementGroup, "")
    }

}
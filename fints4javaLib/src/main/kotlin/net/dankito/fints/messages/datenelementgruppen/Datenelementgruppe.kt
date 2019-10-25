package net.dankito.fints.messages.datenelementgruppen

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.Separators
import net.dankito.fints.messages.datenelemente.DatenelementBase
import java.util.regex.Pattern


abstract class Datenelementgruppe(val dataElements: List<DatenelementBase>, existenzstatus: Existenzstatus)
    : DatenelementBase(existenzstatus) {

    companion object {
        val ReplaceEmptyDataElementsSeparatorsAtEndPattern =
            Pattern.compile("${Separators.DataElementsSeparator}*\$")
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
        val matcher = ReplaceEmptyDataElementsSeparatorsAtEndPattern.matcher(formattedDataElementGroup)

        return matcher.replaceFirst("")
    }

}
package net.dankito.fints.messages.segmente

import net.dankito.fints.messages.Nachrichtenteil
import net.dankito.fints.messages.Separators
import net.dankito.fints.messages.datenelemente.DatenelementBase
import net.dankito.fints.messages.datenelemente.implementierte.DoNotPrintDatenelement
import java.util.regex.Pattern


abstract class Segment(val dataElementsAndGroups: List<DatenelementBase>) : Nachrichtenteil() {

    companion object {
        val ReplaceEmptyDataElementGroupSeparatorsAtEndPattern =
            Pattern.compile("\\${Separators.DataElementGroupsSeparator}*\$")
    }


    override fun format(): String {
        val formattedSegment = dataElementsAndGroups.filter { it is DoNotPrintDatenelement == false }.joinToString(Separators.DataElementGroupsSeparator) { it.format() }

        return cutEmptyDataElementGroupsAtEndOfSegment(formattedSegment)
    }

    /**
     * Auslassen von Datenelementen durch Abschneiden
     * Ist für DE, die am Ende eines Segments stehen, kein Inhalt vorhanden, können sie ausgelassen werden.
     * In diesem Fall wird das Segmentende-Zeichen unmittelbar nach dem letzten mit Inhalt belegten DE angegeben.
     */
    protected open fun cutEmptyDataElementGroupsAtEndOfSegment(formattedSegment: String): String {
        val matcher = ReplaceEmptyDataElementGroupSeparatorsAtEndPattern.matcher(formattedSegment)

        return matcher.replaceFirst("")
    }

}
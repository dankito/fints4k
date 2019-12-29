package net.dankito.fints.messages.datenelemente.implementierte.tan

import net.dankito.fints.messages.datenelemente.implementierte.ICodeEnum


/**
 * dient der Klassifizierung der gesamten dem Kunden zugeordneten TAN-Medien. Bei
 * Geschäftsvorfällen zum Management des TAN-Generators kann aus diesen nach folgender
 * Codierung selektiert werden.
 */
enum class TanMedienArtVersion(override val code: String) : ICodeEnum {

    Alle("0"),

    Aktiv("1"),

    Verfuegbar("2")

}
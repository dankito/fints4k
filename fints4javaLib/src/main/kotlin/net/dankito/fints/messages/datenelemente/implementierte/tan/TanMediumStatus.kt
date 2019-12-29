package net.dankito.fints.messages.datenelemente.implementierte.tan

import net.dankito.fints.messages.datenelemente.implementierte.ICodeEnum


/**
 * Gibt an, in welchem Status sich ein TAN-Medium befindet.
 *
 */
enum class TanMediumStatus(override val code: String) : ICodeEnum {

    /**
     * Die Bank zeigt an, dass es eine TAN-Verifikation gegen dieses Medium vornimmt.
     */
    Aktiv("1"),

    /**
     * Das Medium kann genutzt werden, muss aber zuvor mit „TAN-Generator an- bzw. ummelden (HKTAU)“ aktiv gemeldet werden.
     */
    Verfuegbar("2"),

    /**
     * Mit der ersten Nutzung der Folgekarte wird die zur Zeit aktive Karte gesperrt.
     */
    AktivFolgekarte("3"),

    /**
     * Das Medium kann mit dem Geschäftsvorfall „TAN-Medium an- bzw. ummelden (HKTAU)“ aktiv gemeldet werden.
     * Die aktuelle Karte kann dann nicht mehr genutzt werden.
     */
    VerfuegbarFolgekarte("4")

}
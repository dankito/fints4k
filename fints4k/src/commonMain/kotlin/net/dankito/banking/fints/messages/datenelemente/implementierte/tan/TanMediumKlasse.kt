package net.dankito.banking.fints.messages.datenelemente.implementierte.tan

import net.dankito.banking.fints.messages.datenelemente.implementierte.ICodeEnum


/**
 * dient der Klassifizierung der möglichen TAN-Medien. Bei Geschäftsvorfällen zum
 * Management der TAN-Medien kann aus diesen nach folgender Codierung selektiert werden.
 */
enum class TanMediumKlasse(override val code: String, val supportedHkTabVersions: List<Int>) : ICodeEnum {

    AlleMedien("A", listOf(4, 5)),

    Liste("L", listOf(1, 2, 3, 4, 5)),

    TanGenerator("G", listOf(1, 2, 3, 4, 5)),

    MobiltelefonMitMobileTan("M", listOf(2, 3, 4, 5)),

    Secoder("S", listOf(3, 4, 5)),

    BilateralVereinbart("B", listOf(5))

}
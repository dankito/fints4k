package net.dankito.fints.messages.datenelemente.implementierte.tan


/**
 * Informationen zu Art und Parametrisierung von TAN-Medien. Als TAN-Medien werden sowohl
 * TAN-Listen als auch DK-TAN-Generatoren / Karten oder Mobiltelefone sowie bilateral
 * vereinbarte Medien bezeichnet.
 *
 * Wird das Datenelement „TAN-Medium-Klasse“ mit „B“ (bilateral vereinbart) belegt, so muss im Element „Sicherheitsfunktion, kodiert“ die entsprechende Sicherheitsfunktion in der DEG „Verfahrensparameter Zwei-Schritt-Verfahren“ referenziert werden.
 */
open class TanMedium(
    val mediumClass: TanMediumKlasse,
    val status: TanMediumStatus
) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TanMedium

        if (mediumClass != other.mediumClass) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mediumClass.hashCode()
        result = 31 * result + status.hashCode()
        return result
    }


    override fun toString(): String {
        return "$mediumClass $status"
    }

}
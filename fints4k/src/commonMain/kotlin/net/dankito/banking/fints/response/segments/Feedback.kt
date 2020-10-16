package net.dankito.banking.fints.response.segments


open class Feedback(
    val responseCode: Int,
    val message: String,

    /**
     * Die Position des Datenelements bzw. Gruppendatenelements, auf das sich der Rückmeldungscode bezieht
     * (z. B. die Position des fehlerhaften Elementes bei Syntaxfehlern).
     *
     * Bei Rückmeldecodes, die sich auf eine Nachricht oder ein Segment (Auftrag) beziehen, darf dieses DE nicht
     * belegt werden.
     *
     * Die Angabe des Bezugsdatenelement erlaubt u.U. eine automatische Reaktion des Kundenproduktes. So kann bspw.
     * bei fehlerhaften Eingaben des Kunden direkt auf das betreffende Eingabefeld positioniert werden.
     *
     * Die Referenzierung erfolgt
     * - bei DE durch die Position
     * - bei GD durch die Position der DEG und die Position des GD (die beiden Werte sind durch Komma getrennt)
     *
     * Position des DE:
     * Position des DE = Anzahl der vorstehenden DE-Trennzeichen + 1.
     * Die Anzahl der vorstehenden DE-Trennzeichen ist gleich der Anzahl der vorstehenden DE + Anzahl der
     * vorstehenden DEGs (GD sind nicht separat zuzählen, sondern gehen in die DEGs ein). Entwertete Pluszeichen
     * sind nicht zu zählen.
     *
     * Position des GD innerhalb einer DEG:
     * Position des GD = Anzahl der vorstehenden GD-Trennzeichen innerhalb der DEG + 1
     *
     * Beispiele (Nummern beziehen sich auf das mit |xy| gekennzeichnete Element):
     * Segmentkopf+DE+GD:GD:GD:GD+|DE|+GD:GD’ : 4
     * Segmentkopf+DE+GD:GD:GD:|GD|+DE+GD:GD’ : 3,4
     * Segmentkopf+DE+GD:GD:GD:GD+DE+GD:|GD|’ : 5,2
     */
    val referencedDataElement: String? = null,

    val parameter: String? = null
) {


    val isSuccess: Boolean
        get() = responseCode in 0..999

    val isWarning: Boolean
        get() = responseCode in 3000..3999

    val isPinLocked: Boolean
        get() = responseCode in 3930..3939

    val isError: Boolean
        get() = responseCode in 9000..9999


    override fun toString(): String {
        return "${if (isSuccess) "Success" else if (isWarning) "Warning" else "Error"}: $responseCode $message " +
                if (parameter != null) "($parameter)" else ""
    }

}
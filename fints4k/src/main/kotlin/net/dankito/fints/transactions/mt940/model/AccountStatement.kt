package net.dankito.fints.transactions.mt940.model


open class AccountStatement(

    /**
     * Referenznummer, die vom Sender als eindeutige Kennung für die Nachricht vergeben wurde
     * (z.B. als Referenz auf stornierte Nachrichten).
     *
     * Die Referenz darf nicht mit "/" starten oder enden; darf nicht "//" enthalten
     *
     * Max length = 16
     */
    val transactionReferenceNumber: String,

    /**
     * Bezugsreferenz oder „NONREF“.
     *
     * Die Referenz darf nicht mit "/" starten oder enden; darf nicht "//" enthalten
     *
     * Max length = 16
     */
    val referenceReferenceNumber: String?,

    /**
     * xxxxxxxxxxx/Konto-Nr. oder yyyyyyyy/Konto-Nr.
     * wobei xxxxxxxxxxx = S.W.I.F.T.-Code
     * yyyyyyyy = Bankleitzahl
     * Konto-Nr. = max. 23 Stellen (ggf. mit Währung)
     *
     * Zukünftig kann hier auch die IBAN angegeben werden.
     *
     * Max length = 35
     */
    val bankCodeBicOrIban: String,

    val accountIdentifier: String?,

    /**
     * Falls eine Auszugsnummer nicht unterstützt wird, ist „0“ einzustellen.
     *
     * Max length = 5
     */
    val statementNumber: Int,

    /**
     * „/“ kommt nach statementNumber falls Blattnummer belegt.
     *
     * beginnend mit „1“
     *
     * Max length = 5
     */
    val sequenceNumber: Int?,

    val openingBalance: Balance,

    val transactions: List<Transaction>,

    val closingBalance: Balance,

    val currentValueBalance: Balance? = null,

    val futureValueBalance: Balance? = null,

    /**
     * Mehrzweckfeld
     *
     * Es dürfen nur unstrukturierte Informationen eingestellt werden. Es dürfen keine Informationen,
     * die auf einzelne Umsätze bezogen sind, eingestellt werden.
     *
     * Die Zeilen werden mit <CR><LF> getrennt.
     *
     * Max length = 65
     */
    val multipurposeField: String? = null

) {

    // for object deserializers
    private constructor() : this("", "", "", null, 0, null, Balance(), listOf(), Balance())


    val isStatementNumberSupported: Boolean
        get() = statementNumber != 0


    override fun toString(): String {
        return closingBalance.toString()
    }

}
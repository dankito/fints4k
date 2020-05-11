package net.dankito.fints.transactions.mt940.model

import java.math.BigDecimal
import java.text.DateFormat
import java.util.*


open class Balance(

    val isIntermediate: Boolean,

    /**
     * Soll/Haben-Kennung
     *
     * “C” = Credit (Habensaldo)
     * ”D” = Debit (Sollsaldo)
     *
     * Length = 1
     */
    val isCredit: Boolean,

    /**
     * JJMMTT = Buchungsdatum des Saldos oder '0' beim ersten Auszug
     *
     * Max length = 6
     */
    val bookingDate: Date,

    /**
     * Währungsschlüssel gem. ISO 4217
     *
     * Length = 3
     */
    val currency: String,

    /**
     * Betrag
     *
     * Max Length = 15
     */
    val amount: BigDecimal

) {

    internal constructor() : this(false, false, Date(), "", 0.toBigDecimal()) // for object deserializers


    override fun toString(): String {
        return "${DateFormat.getDateInstance(DateFormat.MEDIUM).format(bookingDate)} ${if (isCredit) "+" else "-"}$amount $currency"
    }

}
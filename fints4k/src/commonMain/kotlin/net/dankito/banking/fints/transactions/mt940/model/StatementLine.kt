package net.dankito.banking.fints.transactions.mt940.model

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.soywiz.klock.Date


open class StatementLine(

    /**
     * Soll/Haben-Kennung
     *
     * “C” = Credit (Habensaldo)
     * ”D” = Debit (Sollsaldo)
     * „RC“ = Storno Haben
     * „RD“ = Storno Soll
     *
     * Max length = 2
     */
    val isCredit: Boolean,

    val isReversal: Boolean,

    /**
     * Valuta (JJMMTT)
     *
     * Length = 6
     */
    val valueDate: Date,

    /**
     * MMTT
     *
     * Length = 4
     */
    val bookingDate: Date?,

    /**
     * dritte Stelle der Währungsbezeichnung, falls sie zur Unterscheidung notwendig ist
     *
     * Length = 1
     */
    val currencyType: String?,

    /**
     * Codes see p. 177 bottom - 179
     *
     * After constant „N“
     *
     * Max length = 15
     */
    val amount: BigDecimal,

    /**
     * in Kontowährung
     *
     * Length = 3
     */
    val bookingKey: String,

    val referenceForTheAccountOwner: String,

    val referenceOfTheAccountServicingInstitution: String?,

    val supplementaryDetails: String? = null

) {

    override fun toString(): String {
        return "$valueDate ${if (isCredit) "+" else "-"}$amount"
    }

}
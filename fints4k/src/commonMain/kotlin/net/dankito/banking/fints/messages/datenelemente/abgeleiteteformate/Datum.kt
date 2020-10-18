package net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.NumerischesDatenelement
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.DateFormatter


/**
 * Format: JJJJMMTT gemäß ISO 8601
 *
 * Erlaubt sind alle existenten Datumsangaben.
 */
open class Datum(date: Int?, existenzstatus: Existenzstatus) : NumerischesDatenelement(date, 8, existenzstatus) {

    companion object {
        const val HbciDateFormatString = "yyyyMMdd"

        val HbciDateFormat = DateFormatter(HbciDateFormatString)


        fun format(date: Date): String {
            return HbciDateFormat.format(date) // TODO: is this correct?
        }

        fun parse(dateString: String): Date {
            return HbciDateFormat.parse(dateString) !!
        }
    }


    constructor(date: Date?, existenzstatus: Existenzstatus)
            : this(date?.let { format(it).toInt() }, existenzstatus)

}
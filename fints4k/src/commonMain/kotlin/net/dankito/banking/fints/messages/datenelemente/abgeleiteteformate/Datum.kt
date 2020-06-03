package net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate

import com.soywiz.klock.Date
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Format: JJJJMMTT gemäß ISO 8601
 *
 * Erlaubt sind alle existenten Datumsangaben.
 */
open class Datum(date: Int?, existenzstatus: Existenzstatus) : NumerischesDatenelement(date, 8, existenzstatus) {

    companion object {
        const val HbciDateFormatString = "yyyyMMdd"

        val HbciDateFormat = DateFormat(HbciDateFormatString)


        fun format(date: Date): String {
            return HbciDateFormat.format(date.dateTimeDayStart.localUnadjusted) // TODO: is this correct?
        }

        fun parse(dateString: String): Date {
            return HbciDateFormat.parse(dateString).utc.date // TODO: really use UTC?
        }
    }


    constructor(date: Date?, existenzstatus: Existenzstatus)
            : this(date?.let { format(it).toInt() }, existenzstatus)

}
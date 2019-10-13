package net.dankito.fints.messages.datenelemente.abgeleiteteformate

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.NumerischesDatenelement
import java.text.SimpleDateFormat
import java.util.*


/**
 * Format: JJJJMMTT gemäß ISO 8601
 *
 * Erlaubt sind alle existenten Datumsangaben.
 */
open class Datum(date: Int, existenzstatus: Existenzstatus) : NumerischesDatenelement(date, 8, existenzstatus) {

    companion object {
        const val HbciDateFormatString = "yyyyMMdd"

        val HbciDateFormat = SimpleDateFormat(HbciDateFormatString)

        const val DateNotSet = Int.MIN_VALUE
    }


    constructor(date: Date?, existenzstatus: Existenzstatus)
            : this(date?.let { HbciDateFormat.format(it).toInt() } ?: DateNotSet, existenzstatus)


    override fun format(): String {
        if (value == DateNotSet) {
            return "" // optional element and value not set -> write nothing to output
        }

        return super.format()
    }

}
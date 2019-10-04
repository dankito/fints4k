package net.dankito.fints.messages.datenelemente.abgeleiteteformate

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Format: JJJJMMTT gemäß ISO 8601
 *
 * Erlaubt sind alle existenten Datumsangaben.
 */
open class Datum(date: Int, existenzstatus: Existenzstatus) : NumerischesDatenelement(date, 8, existenzstatus) {

    companion object {
        const val HbciDateFormat = "yyyyMMdd"
    }

}
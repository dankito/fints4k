package net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.ZiffernDatenelement
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.DateFormatter


/**
 * Format: hhmmss gemäß ISO 8601
 *
 * Gültige Uhrzeit. Es ist immer Ortszeit des sendenden Systems einzustellen.
 * Unterschiedliche Zeitzonen werden nicht unterstützt
 */
open class Uhrzeit(time: Int?, existenzstatus: Existenzstatus) : ZiffernDatenelement(time, 6, existenzstatus) {

    companion object {
        const val HbciTimeFormatString = "HHmmss"

        val HbciTimeFormat = DateFormatter(HbciTimeFormatString)


        fun format(time: Date): String {
            return HbciTimeFormat.format(time) // TODO: is this correct?
        }

        fun parse(dateString: String): Date {
            return HbciTimeFormat.parse(dateString) !!
        }
    }


    constructor(time: Date?, existenzstatus: Existenzstatus)
            : this(time?.let { format(time).toInt() }, existenzstatus)

}
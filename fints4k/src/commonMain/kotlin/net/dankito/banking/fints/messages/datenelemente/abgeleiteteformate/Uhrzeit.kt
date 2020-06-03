package net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate

import com.soywiz.klock.*
import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.ZiffernDatenelement


/**
 * Format: hhmmss gemäß ISO 8601
 *
 * Gültige Uhrzeit. Es ist immer Ortszeit des sendenden Systems einzustellen.
 * Unterschiedliche Zeitzonen werden nicht unterstützt
 */
open class Uhrzeit(time: Int?, existenzstatus: Existenzstatus) : ZiffernDatenelement(time, 6, existenzstatus) {

    companion object {
        const val HbciTimeFormatString = "HHmmss"

        val HbciTimeFormat = DateFormat(HbciTimeFormatString)


        fun format(time: Time): String {
            return HbciTimeFormat.format(DateTimeTz.Companion.fromUnixLocal(time.encoded.milliseconds)) // TODO: is this correct?
        }

        fun parse(dateString: String): Time {
            return HbciTimeFormat.parse(dateString).utc.time // TODO: is this correct?
        }
    }


    constructor(time: Time?, existenzstatus: Existenzstatus)
            : this(time?.let { format(time).toInt() }, existenzstatus)

}
package net.dankito.fints.messages.datenelemente.abgeleiteteformate

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.ZiffernDatenelement
import java.text.SimpleDateFormat
import java.util.*


/**
 * Format: hhmmss gemäß ISO 8601
 *
 * Gültige Uhrzeit. Es ist immer Ortszeit des sendenden Systems einzustellen.
 * Unterschiedliche Zeitzonen werden nicht unterstützt
 */
open class Uhrzeit(time: Int?, existenzstatus: Existenzstatus) : ZiffernDatenelement(time, 6, existenzstatus) {

    companion object {
        const val HbciTimeFormatString = "HHmmss"

        val HbciTimeFormat = SimpleDateFormat(HbciTimeFormatString)
    }


    constructor(time: Date?, existenzstatus: Existenzstatus)
            : this(time?.let { HbciTimeFormat.format(it).toInt() }, existenzstatus)

}
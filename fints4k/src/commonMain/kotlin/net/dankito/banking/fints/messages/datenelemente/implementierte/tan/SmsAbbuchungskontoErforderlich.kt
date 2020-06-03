package net.dankito.banking.fints.messages.datenelemente.implementierte.tan

import net.dankito.banking.fints.messages.datenelemente.implementierte.ICodeEnum


/**
 * Parameter, der angibt, ob eine Zahlungsverkehrskontoverbindung für die Abbuchung von SMS-Kosten angegeben werden
 * kann oder muss. Die Belastung von SMS-Kosten durch das Institut wird unabhängig von dem Vorhandensein einer
 * Kontoverbindung z. B. kundenindividuell geregelt.
 *
 * Das Element in der Version #2 ermöglicht eine detailliertere Steuerung der Belegung. Es wird z. B. in HKTAN
 * ab Segmentversion #5 eingesetzt.
 */
enum class SmsAbbuchungskontoErforderlich(override val code: String) : ICodeEnum {

    SmsAbbuchungskontoDarfNichtAngegebenWerden("0"),

    SmsAbbuchungskontoKannAngegebenWerden("1"),

    SmsAbbuchungskontoMussAngegebenWerden("2")

}
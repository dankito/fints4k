package net.dankito.fints.messages.datenelemente.implementierte.tan

import net.dankito.fints.messages.datenelemente.implementierte.ICodeEnum


/**
 * Parameter, der angibt, ob eine Zahlungsverkehrskontoverbindung explizit angegeben werden muss, wenn diese
 * im Geschäftsvorfall enthalten ist.
 *
 * Diese Funktion ermöglicht das Sicherstellen einer gültigen Kontoverbindung z. B. für die Abrechnung von
 * SMS-Kosten bereits vor Erzeugen und Versenden einer (ggf. kostenpflichtigen!) TAN.
 */
enum class AuftraggeberkontoErforderlich(override val code: String) : ICodeEnum {

    AuftraggeberkontoDarfNichtAngegebenWerden("0"),

    AuftraggeberkontoMussAngegebenWerdenWennImGeschaeftsvorfallEnthalten("2")

}
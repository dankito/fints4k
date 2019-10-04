package net.dankito.fints.messages.datenelemente.implementierte.tan

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Symbolischer Name für ein TAN-Medium wie z. B. TAN-Generator oder Mobiltelefon. Diese Bezeichnung
 * kann in Verwaltungs-Geschäftsvorfällen benutzt werden, wenn z. B. die Angabe der echten Handynummer
 * aus Datenschutzgründen nicht möglich ist oder auch um die Benutzerfreundlichkeit zu erhöhen.
 */
open class BezeichnungDesTANMediums(identifier: String, existenzstatus: Existenzstatus)
    : AlphanumerischesDatenelement(identifier, existenzstatus, 32)
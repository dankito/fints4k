package net.dankito.fints.messages.datenelemente.implementierte.sepa

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.BinaerDatenelement
import net.dankito.fints.messages.segmente.implementierte.sepa.ISepaMessageCreator


open class SepaMessage(
    filename: String,
    replacementStrings: Map<String, String>,
    messageCreator: ISepaMessageCreator
)
    : BinaerDatenelement(messageCreator.createXmlFile(filename, replacementStrings), Existenzstatus.Mandatory) {

}



package net.dankito.banking.fints.messages.datenelemente.implementierte.sepa

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.BinaerDatenelement
import net.dankito.banking.fints.messages.segmente.implementierte.sepa.ISepaMessageCreator


open class SepaMessage(
    filename: String,
    replacementStrings: Map<String, String>,
    messageCreator: ISepaMessageCreator
)
    : BinaerDatenelement(messageCreator.createXmlFile(filename, replacementStrings), Existenzstatus.Mandatory) {

}



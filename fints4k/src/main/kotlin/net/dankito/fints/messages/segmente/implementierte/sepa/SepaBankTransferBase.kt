package net.dankito.fints.messages.segmente.implementierte.sepa

import net.dankito.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.fints.model.AccountData
import net.dankito.fints.model.BankTransferData
import net.dankito.fints.model.CustomerData


open class SepaBankTransferBase(
    segmentId: CustomerSegmentId,
    segmentNumber: Int,
    sepaDescriptorUrn: String,
    debitor: CustomerData,
    account: AccountData,
    debitorBic: String,
    data: BankTransferData,
    messageCreator: ISepaMessageCreator = SepaMessageCreator()
)
    : SepaSegment(
    segmentNumber,
    segmentId,
    1,
    sepaDescriptorUrn,
    if (sepaDescriptorUrn.contains("pain.001.003.03", true)) "pain.001.003.03.xml" else "pain.001.001.03.xml",
    account.iban ?: "", // TODO: what to do if iban is not set?
    debitorBic,
    mapOf(
        SepaMessageCreator.NumberOfTransactionsKey to "1", // TODO: may someday support more then one transaction per file
        "DebitorName" to messageCreator.convertToAllowedCharacters(debitor.name),
        "DebitorIban" to account.iban!!,
        "DebitorBic" to debitorBic,
        "CreditorName" to messageCreator.convertToAllowedCharacters(data.creditorName),
        "CreditorIban" to data.creditorIban.replace(" ", ""),
        "CreditorBic" to data.creditorBic.replace(" ", ""),
        "Amount" to data.amount.toString(), // TODO: check if ',' or '.' should be used as decimal separator
        "Usage" to messageCreator.convertToAllowedCharacters(data.usage),
        "RequestedExecutionDate" to RequestedExecutionDateValueForNotScheduledTransfers
    ),
    messageCreator
) {

    companion object {
        /**
         * In das Mussfeld RequestedExecutionDate <ReqdExctnDt> ist der 1999-01-01 einzustellen.
         */
        const val RequestedExecutionDateValueForNotScheduledTransfers = "1999-01-01"
    }
}
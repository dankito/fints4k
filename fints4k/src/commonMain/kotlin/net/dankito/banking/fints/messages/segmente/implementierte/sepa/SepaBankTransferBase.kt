package net.dankito.banking.fints.messages.segmente.implementierte.sepa

import net.dankito.banking.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.banking.fints.model.AccountData
import net.dankito.banking.fints.model.BankTransferData


open class SepaBankTransferBase(
    segmentId: CustomerSegmentId,
    segmentNumber: Int,
    sepaDescriptorUrn: String,
    debitorName: String,
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
    if (sepaDescriptorUrn.contains("pain.001.003.03", true)) PaymentInformationMessages.Pain_001_003_03 else PaymentInformationMessages.Pain_001_001_03,
    account,
    debitorBic,
    mapOf(
        SepaMessageCreator.NumberOfTransactionsKey to "1", // TODO: may someday support more then one transaction per file
        "DebitorName" to messageCreator.convertDiacriticsAndReservedXmlCharacters(debitorName),
        "DebitorIban" to account.iban!!,
        "DebitorBic" to debitorBic,
        "RecipientName" to messageCreator.convertDiacriticsAndReservedXmlCharacters(data.recipientName),
        "RecipientIban" to data.recipientAccountId.replace(" ", ""),
        "RecipientBic" to data.recipientBankCode.replace(" ", ""),
        "Amount" to data.amount.amount.string.replace(',', '.'), // TODO: check if ',' or '.' should be used as decimal separator
        "Reference" to if (data.reference.isEmpty()) " " else messageCreator.convertDiacriticsAndReservedXmlCharacters(data.reference),
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
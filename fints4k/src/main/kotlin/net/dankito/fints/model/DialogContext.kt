package net.dankito.fints.model

import net.dankito.fints.messages.MessageBuilderResult
import net.dankito.fints.response.Response


open class DialogContext(
    bank: BankData,
    customer: CustomerData,
    product: ProductData,
    var currentMessage: MessageBuilderResult? = null,
    var dialogId: String = InitialDialogId,
    var messageNumber: Int = InitialMessageNumber,
    var response: Response? = null,
    var previousMessageInDialog: MessageBuilderResult? = null
) : MessageBaseData(bank, customer, product) {

    companion object {
        const val InitialDialogId = "0"

        const val InitialMessageNumber = 1
    }


    fun increaseMessageNumber() {
        messageNumber++
    }

}
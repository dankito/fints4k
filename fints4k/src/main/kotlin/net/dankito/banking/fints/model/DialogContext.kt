package net.dankito.banking.fints.model

import net.dankito.banking.fints.messages.MessageBuilderResult
import net.dankito.banking.fints.response.Response


open class DialogContext(
    bank: BankData,
    customer: CustomerData,
    product: ProductData,
    var currentMessage: MessageBuilderResult? = null,
    var dialogId: String = InitialDialogId,
    var response: Response? = null,
    var didBankCloseDialog: Boolean = false,
    var previousMessageInDialog: MessageBuilderResult? = null
) : MessageBaseData(bank, customer, product) {

    companion object {
        const val InitialDialogId = "0"

        const val InitialMessageNumber = 0
    }

    open var messageNumber: Int = InitialMessageNumber
        protected set

    open fun increaseMessageNumber() {
        messageNumber++
    }

}
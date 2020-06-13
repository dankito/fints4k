package net.dankito.banking.ui.model.moneytransfer


open class ExtractTransferMoneyDataFromPdfResult(
    val type: ExtractTransferMoneyDataFromPdfResultType,
    val error: Exception? = null
) {

    override fun toString(): String {
        return type.toString()
    }

}
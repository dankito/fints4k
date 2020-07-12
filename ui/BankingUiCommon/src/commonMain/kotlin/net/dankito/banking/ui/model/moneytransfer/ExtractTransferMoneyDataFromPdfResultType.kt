package net.dankito.banking.ui.model.moneytransfer


enum class ExtractTransferMoneyDataFromPdfResultType {

    Success,

    NotASearchablePdf,

    CouldNotExtractText,

    CouldNotExtractInvoiceDataFromExtractedText

}
package net.dankito.banking.util.extraction


open class NoOpInvoiceDataExtractor : IInvoiceDataExtractor {

    override fun extractInvoiceData(text: String): InvoiceData {
        return InvoiceData(null, null, null, null, null)
    }

}
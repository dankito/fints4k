package net.dankito.banking.util.extraction

import net.dankito.utils.multiplatform.toBigDecimal
import net.dankito.text.extraction.info.invoice.InvoiceDataExtractor


open class JavaInvoiceDataExtractor(
    protected val invoiceDataExtractor: net.dankito.text.extraction.info.invoice.IInvoiceDataExtractor = InvoiceDataExtractor()
) : IInvoiceDataExtractor {

    override fun extractInvoiceData(text: String): InvoiceData {
        val invoiceData = invoiceDataExtractor.extractInvoiceData(text)

        return InvoiceData(
            invoiceData.potentialTotalAmount?.amount?.toBigDecimal(),
            invoiceData.potentialTotalAmount?.currency,
            null,
            null,
            invoiceData.error
        )
    }

}
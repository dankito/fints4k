package net.dankito.banking.util.extraction


interface IInvoiceDataExtractor {

    fun extractInvoiceData(text: String): InvoiceData

}
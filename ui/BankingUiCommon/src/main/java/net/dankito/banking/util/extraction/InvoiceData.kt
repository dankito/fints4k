package net.dankito.banking.util.extraction

import java.math.BigDecimal


open class InvoiceData(
    open val potentialTotalAmount: BigDecimal?,
    open val potentialCurrency: String?,
    open val potentialIban: String?,
    open val potentialBic: String?,
    open val error: Exception? = null
) {
}
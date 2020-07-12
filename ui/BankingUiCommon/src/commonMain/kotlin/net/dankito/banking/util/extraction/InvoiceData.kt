package net.dankito.banking.util.extraction

import net.dankito.utils.multiplatform.BigDecimal


open class InvoiceData(
    open val potentialTotalAmount: BigDecimal?,
    open val potentialCurrency: String?,
    open val potentialIban: String?,
    open val potentialBic: String?,
    open val error: Exception? = null
) {
}
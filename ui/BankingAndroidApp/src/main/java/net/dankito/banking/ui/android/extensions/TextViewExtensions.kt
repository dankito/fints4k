package net.dankito.banking.ui.android.extensions

import android.widget.TextView
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.android.extensions.setTextColorToColorResource
import net.dankito.utils.multiplatform.BigDecimal


fun TextView.showAmount(presenter: BankingPresenter, amount: BigDecimal) {
    text = presenter.formatAmount(amount)
    setTextColorForAmount(amount)
}

fun TextView.setTextColorForAmount(amount: BigDecimal) {
    setTextColorToColorResource(if (amount >= java.math.BigDecimal.ZERO) R.color.positiveAmount else R.color.negativeAmount)
}
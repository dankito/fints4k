package net.dankito.banking.ui.model.parameters

import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.banking.ui.model.AccountTransaction


open class TransferMoneyData(
    val creditorName: String,
    val creditorIban: String,
    val creditorBic: String,
    val amount: BigDecimal,
    val usage: String,
    val instantPayment: Boolean = false
) {

    companion object {

        fun fromAccountTransaction(transaction: AccountTransaction): TransferMoneyData {
            return TransferMoneyData(
                transaction.otherPartyName ?: "",
                transaction.otherPartyAccountId ?: "",
                transaction.otherPartyBankCode ?: "",
                BigDecimal.Zero,
                ""
            )
        }

    }

}
package net.dankito.banking.ui.model.parameters

import net.dankito.banking.ui.model.*
import net.dankito.utils.multiplatform.BigDecimal


open class TransferMoneyData(
    val account: TypedBankAccount,
    val creditorName: String,
    val creditorIban: String,
    val creditorBic: String,
    val amount: BigDecimal,
    val usage: String,
    val instantPayment: Boolean = false
) {

    companion object {

        fun fromAccountTransactionWithoutAmountAndUsage(transaction: IAccountTransaction): TransferMoneyData {
            return TransferMoneyData(
                transaction.account as TypedBankAccount,
                transaction.otherPartyName ?: "",
                transaction.otherPartyAccountId ?: "",
                transaction.otherPartyBankCode ?: "",
                BigDecimal.Zero,
                ""
            )
        }

        fun fromAccountTransaction(transaction: IAccountTransaction): TransferMoneyData {
            return TransferMoneyData(
                transaction.account as TypedBankAccount,
                transaction.otherPartyName ?: "",
                transaction.otherPartyAccountId ?: "",
                transaction.otherPartyBankCode ?: "",
                if (transaction.amount.isPositive) transaction.amount else transaction.amount.negated(),
                transaction.usage
            )
        }

    }

}
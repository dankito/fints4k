package net.dankito.banking.ui.model.parameters

import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount


open class TransferMoneyData(
    val account: BankAccount,
    val creditorName: String,
    val creditorIban: String,
    val creditorBic: String,
    val amount: BigDecimal,
    val usage: String,
    val instantPayment: Boolean = false
) {

    companion object {

        fun fromAccountTransactionWithoutAmountAndUsage(transaction: AccountTransaction): TransferMoneyData {
            return TransferMoneyData(
                transaction.bankAccount,
                transaction.otherPartyName ?: "",
                transaction.otherPartyAccountId ?: "",
                transaction.otherPartyBankCode ?: "",
                BigDecimal.Zero,
                ""
            )
        }

        fun fromAccountTransaction(transaction: AccountTransaction): TransferMoneyData {
            return TransferMoneyData(
                transaction.bankAccount,
                transaction.otherPartyName ?: "",
                transaction.otherPartyAccountId ?: "",
                transaction.otherPartyBankCode ?: "",
                if (transaction.amount.isPositive) transaction.amount else transaction.amount.negated(),
                transaction.usage
            )
        }

    }

}
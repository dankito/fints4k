package net.dankito.banking.ui.model.parameters

import net.dankito.banking.ui.model.*
import net.dankito.utils.multiplatform.BigDecimal


open class TransferMoneyData(
    val account: TypedBankAccount,
    val recipientName: String,
    val recipientAccountId: String,
    val recipientBankCode: String,
    val amount: BigDecimal,
    val reference: String,
    val realTimeTransfer: Boolean = false
) {

    companion object {

        fun fromAccountTransactionWithoutAmountAndReference(transaction: IAccountTransaction): TransferMoneyData {
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
                transaction.reference
            )
        }

    }

}
package net.dankito.banking.fints.model


fun BankData.toAddAccountParameter(): AddAccountParameter {
    return toAddAccountParameter(true)
}

fun BankData.toAddAccountParameter(fetchBalanceAndTransactions: Boolean = true): AddAccountParameter {
    return AddAccountParameter(this, fetchBalanceAndTransactions)
}
package net.dankito.banking.fints.transactions.mt940.model


open class Transaction(

    val turnover: Turnover,
    val details: TransactionDetails? = null

) {

    override fun toString(): String {
        return "$turnover ($details)"
    }

}
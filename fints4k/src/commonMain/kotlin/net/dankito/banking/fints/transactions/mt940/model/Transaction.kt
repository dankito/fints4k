package net.dankito.banking.fints.transactions.mt940.model


open class Transaction(

    val statementLine: StatementLine,
    val information: InformationToAccountOwner? = null

) {

    override fun toString(): String {
        return "$statementLine ($information)"
    }

}
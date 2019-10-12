package net.dankito.fints.transactions

import net.dankito.fints.model.AccountTransaction
import net.dankito.fints.transactions.mt940.IMt940Parser
import net.dankito.fints.transactions.mt940.Mt940Parser
import net.dankito.fints.transactions.mt940.model.AccountStatement
import net.dankito.fints.transactions.mt940.model.Balance
import net.dankito.fints.transactions.mt940.model.Transaction
import net.dankito.fints.transactions.mt940.model.Turnover
import org.slf4j.LoggerFactory
import java.math.BigDecimal


open class Mt940AccountTransactionsParser @JvmOverloads constructor(
    protected val mt940Parser: IMt940Parser = Mt940Parser()
) : IAccountTransactionsParser {

    companion object {
        private val log = LoggerFactory.getLogger(Mt940AccountTransactionsParser::class.java)
    }


    override fun parseTransactions(transactionsString: String): List<AccountTransaction> {
        val accountStatements = mt940Parser.parseMt940String(transactionsString)

        return accountStatements.flatMap { mapToAccountTransactions(it) }
    }

    protected open fun mapToAccountTransactions(statement: AccountStatement): List<AccountTransaction> {
        try {
            return statement.transactions.map { mapToAccountTransaction(statement, it) }
        } catch (e: Exception) {
            log.error("Could not map AccountStatement '$statement' to AccountTransactions", e)
        }

        return listOf()
    }

    protected open fun mapToAccountTransaction(statement: AccountStatement, transaction: Transaction): AccountTransaction {
        return AccountTransaction(
            mapAmount(transaction.turnover),
            statement.closingBalance.currency,
            transaction.details?.sepaUsage ?: transaction.details?.usage ?: "",
            transaction.turnover.bookingDate ?: statement.closingBalance.bookingDate,
            transaction.details?.otherPartyName,
            transaction.details?.otherPartyBankCode,
            transaction.details?.otherPartyAccountId,
            transaction.details?.bookingText,
            transaction.turnover.valueDate,
            mapAmount(statement.openingBalance),
            mapAmount(statement.closingBalance)
        )
    }

    /**
     * In MT940 amounts are always stated as a positive number and flag isCredit decides if it's a credit or debit.
     */
    protected open fun mapAmount(balance: Balance): BigDecimal {
        return mapAmount(balance.amount, balance.isCredit)
    }

    /**
     * In MT940 amounts are always stated as a positive number and flag isCredit decides if it's a credit or debit.
     */
    protected open fun mapAmount(turnover: Turnover): BigDecimal {
        return mapAmount(turnover.amount, turnover.isCredit)
    }

    /**
     * In MT940 amounts are always stated as a positive number and flag isCredit decides if it's a credit or debit.
     */
    protected open fun mapAmount(positiveAmount: BigDecimal, isCredit: Boolean): BigDecimal {
        if (isCredit == false) {
            return positiveAmount.negate()
        }

        return positiveAmount
    }

}
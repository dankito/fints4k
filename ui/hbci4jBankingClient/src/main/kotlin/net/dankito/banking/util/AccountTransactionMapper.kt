package net.dankito.banking.util

import net.dankito.banking.fints.transactions.mt940.Mt940Parser
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import org.kapott.hbci.GV_Result.GVRKUms
import org.kapott.hbci.structures.Value
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.text.SimpleDateFormat


open class AccountTransactionMapper {

    companion object {
        protected val DateStartString = "DATUM "
        protected val DateEndString = " UHR"

        protected val DateTimeFormat = SimpleDateFormat("dd.MM.yyyy,HH.mm")

        protected val DateFormat = SimpleDateFormat("dd.MM.yyyy,")

        private val log = LoggerFactory.getLogger(AccountTransactionMapper::class.java)
    }


    open fun mapAccountTransactions(bankAccount: BankAccount, result: GVRKUms): List<AccountTransaction> {
        val entries = ArrayList<AccountTransaction>()

        result.dataPerDay.forEach { btag ->
            btag.lines.forEach { transaction ->
                entries.add(mapAccountingEntry(bankAccount, btag, transaction))
            }
        }

        log.debug("Retrieved ${result.flatData.size} accounting entries")

        return entries.sortedByDescending { it.bookingDate }
    }

    protected open fun mapAccountingEntry(bankAccount: BankAccount, btag: GVRKUms.BTag, transaction: GVRKUms.UmsLine): AccountTransaction {
        val unparsedUsage = transaction.usage.joinToString("")
        val parsedUsage = Mt940Parser().getUsageParts(unparsedUsage)
        val statementAndMaySequenceNumber = btag.counter.split('/')

        val result = AccountTransaction(bankAccount,
            mapValue(transaction.value), transaction.value.curr, unparsedUsage, transaction.bdate,
            transaction.other.name + (transaction.other.name2 ?: ""),
            transaction.other.bic ?: transaction.other.blz,
            transaction.other.iban ?: transaction.other.number,
            transaction.text, transaction.valuta,
            statementAndMaySequenceNumber[0].toInt(),
            if (statementAndMaySequenceNumber.size > 1) statementAndMaySequenceNumber[1].toInt() else null,
            mapValue(btag.start.value), mapValue(btag.end.value),

            parsedUsage[Mt940Parser.EndToEndReferenceUsageKey],
            parsedUsage[Mt940Parser.CustomerReferenceUsageKey],
            parsedUsage[Mt940Parser.MandateReferenceUsageKey],
            parsedUsage[Mt940Parser.CreditorIdentifierUsageKey],
            parsedUsage[Mt940Parser.OriginatorsIdentificationCodeUsageKey],
            parsedUsage[Mt940Parser.CompensationAmountUsageKey],
            parsedUsage[Mt940Parser.OriginalAmountUsageKey],
            parsedUsage[Mt940Parser.SepaUsageUsageKey],
            parsedUsage[Mt940Parser.DeviantOriginatorUsageKey],
            parsedUsage[Mt940Parser.DeviantRecipientUsageKey],
            parsedUsage[""],
            transaction.primanota,
            transaction.addkey,

            null,
            "",
            transaction.customerref,
            transaction.instref,
            transaction.additional,

            "",
            null
        )

        return result
    }

    protected open fun mapValue(value: Value): BigDecimal {
        return BigDecimal.valueOf(value.longValue).divide(BigDecimal.valueOf(100))
    }

}
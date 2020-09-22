package net.dankito.banking.util

import net.dankito.banking.fints.transactions.mt940.Mt940Parser
import net.dankito.banking.ui.model.TypedBankAccount
import net.dankito.banking.ui.model.IAccountTransaction
import net.dankito.banking.ui.model.mapper.IModelCreator
import net.dankito.utils.multiplatform.toDate
import org.kapott.hbci.GV_Result.GVRKUms
import org.kapott.hbci.structures.Value
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.text.SimpleDateFormat


open class AccountTransactionMapper(
    protected val modelCreator: IModelCreator
) {

    companion object {
        protected val DateStartString = "DATUM "
        protected val DateEndString = " UHR"

        protected val DateTimeFormat = SimpleDateFormat("dd.MM.yyyy,HH.mm")

        protected val DateFormat = SimpleDateFormat("dd.MM.yyyy,")

        private val log = LoggerFactory.getLogger(AccountTransactionMapper::class.java)
    }


    open fun mapTransactions(account: TypedBankAccount, result: GVRKUms): List<IAccountTransaction> {
        val entries = mutableListOf<IAccountTransaction>()

        result.dataPerDay.forEach { btag ->
            btag.lines.forEach { transaction ->
                entries.add(mapTransaction(account, btag, transaction))
            }
        }

        log.debug("Retrieved ${result.flatData.size} account transactions")

        return entries
    }

    protected open fun mapTransaction(account: TypedBankAccount, btag: GVRKUms.BTag, transaction: GVRKUms.UmsLine): IAccountTransaction {
        val unparsedUsage = transaction.usage.joinToString("")
        val parsedUsage = Mt940Parser().getUsageParts(unparsedUsage)
        val statementAndMaySequenceNumber = btag.counter.split('/')

        return modelCreator.createTransaction(account,
            mapValue(transaction.value), transaction.value.curr, unparsedUsage, transaction.bdate.toDate(),
            transaction.other.name + (transaction.other.name2 ?: ""),
            transaction.other.bic ?: transaction.other.blz,
            transaction.other.iban ?: transaction.other.number,
            transaction.text, transaction.valuta.toDate(),
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
    }

    protected open fun mapValue(value: Value): net.dankito.utils.multiplatform.BigDecimal {
        return net.dankito.utils.multiplatform.BigDecimal(BigDecimal.valueOf(value.longValue).divide(BigDecimal.valueOf(100)).toPlainString())
    }

}
package net.dankito.banking.util

import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.mapper.IModelCreator
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.toBigDecimal
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.tan.TanProcedureType
import org.kapott.hbci.passport.HBCIPassport
import org.kapott.hbci.structures.Konto
import org.kapott.hbci.structures.Value


open class hbci4jModelMapper(
    protected val modelCreator: IModelCreator
) {

    open fun mapToKonto(bankAccount: TypedBankAccount): Konto {
        val customer = bankAccount.customer

        val konto = Konto("DE", customer.bankCode, bankAccount.identifier, bankAccount.subAccountNumber)

        konto.name = customer.bankName
        konto.iban = bankAccount.iban
        konto.bic = customer.bic

        return konto
    }

    open fun mapToKonto(data: TransferMoneyData): Konto {
        return mapToKonto(data.creditorName, data.creditorIban, data.creditorBic)
    }

    open fun mapToKonto(accountHolderName: String, iban: String, bic: String): Konto {
        val konto = Konto()

        konto.name = accountHolderName
        konto.iban = iban
        konto.bic = bic

        return konto
    }


    open fun mapBankAccounts(customer: TypedCustomer, bankAccounts: Array<out Konto>, passport: HBCIPassport): List<TypedBankAccount> {
        return bankAccounts.map { bankAccount ->
            val iban = if (bankAccount.iban.isNullOrBlank() == false) bankAccount.iban else passport.upd.getProperty("KInfo.iban") ?: ""

            val result = modelCreator.createBankAccount(customer, bankAccount.number,
                if (bankAccount.name2.isNullOrBlank() == false) bankAccount.name + " " + bankAccount.name2 else bankAccount.name)

            result.iban = iban
            result.subAccountNumber = bankAccount.subnumber
            result.customerId = bankAccount.customerid

            result.currency = bankAccount.curr
            result.type = mapBankAccountType(bankAccount)
            result.accountLimit = bankAccount.limit?.value?.let { mapValue(it).toString() }

            result.supportsRetrievingBalance = bankAccount.allowedGVs.contains("HKSAL")
            result.supportsRetrievingAccountTransactions = bankAccount.allowedGVs.contains("HKKAZ")
            result.supportsRetrievingBalance = bankAccount.allowedGVs.contains("HKCCS")

            result
        }
    }

    open fun mapBankAccountType(bankAccount: Konto): BankAccountType {
        val type = bankAccount.acctype

        return when {
            type.length == 1 -> BankAccountType.Girokonto
            type.startsWith("1") -> BankAccountType.Sparkonto
            type.startsWith("2") -> BankAccountType.Festgeldkonto
            type.startsWith("3") -> BankAccountType.Wertpapierdepot
            type.startsWith("4") -> BankAccountType.Darlehenskonto
            type.startsWith("5") -> BankAccountType.Kreditkartenkonto
            type.startsWith("6") -> BankAccountType.FondsDepot
            type.startsWith("7") -> BankAccountType.Bausparvertrag
            type.startsWith("8") -> BankAccountType.Versicherungsvertrag
            type.startsWith("9") -> BankAccountType.Sonstige
            else -> BankAccountType.Sonstige
        }
    }

    protected open fun mapValue(value: Value): BigDecimal {
        return java.math.BigDecimal.valueOf(value.longValue).divide(java.math.BigDecimal.valueOf(100)).toBigDecimal()
    }


    open fun mapTanProcedures(tanProceduresString: String): List<net.dankito.banking.ui.model.tan.TanProcedure> {
        return tanProceduresString.split('|')
            .map { mapTanProcedure(it) }
            .filterNotNull()
    }

    open fun mapTanProcedure(tanProcedureString: String): net.dankito.banking.ui.model.tan.TanProcedure? {
        val parts = tanProcedureString.split(':')

        if (parts.size > 1) {
            val code = parts[0]
            val displayName = parts[1]
            val displayNameLowerCase = displayName.toLowerCase()

            return when {
                // TODO: implement all TAN procedures
                displayNameLowerCase.contains("chiptan") -> {
                    if (displayNameLowerCase.contains("qr")) {
                        modelCreator.createTanProcedure(displayName, TanProcedureType.ChipTanQrCode, code)
                    }
                    else {
                        modelCreator.createTanProcedure(displayName, TanProcedureType.ChipTanFlickercode, code)
                    }
                }

                displayNameLowerCase.contains("sms") -> modelCreator.createTanProcedure(displayName, TanProcedureType.SmsTan, code)
                displayNameLowerCase.contains("push") -> modelCreator.createTanProcedure(displayName, TanProcedureType.AppTan, code)

                // we filter out iTAN and Einschritt-Verfahren as they are not permitted anymore according to PSD2
                else -> null
            }
        }

        return null
    }

}
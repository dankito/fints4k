package net.dankito.banking.util

import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.mapper.IModelCreator
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.toBigDecimal
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.tan.TanMethodType
import org.kapott.hbci.passport.HBCIPassport
import org.kapott.hbci.structures.Konto
import org.kapott.hbci.structures.Value


open class hbci4jModelMapper(
    protected val modelCreator: IModelCreator
) {

    open fun mapToKonto(account: TypedBankAccount): Konto {
        val bank = account.bank

        val konto = Konto("DE", bank.bankCode, account.identifier, account.subAccountNumber)

        konto.name = bank.bankName
        konto.iban = account.iban
        konto.bic = bank.bic

        return konto
    }

    open fun mapToKonto(data: TransferMoneyData): Konto {
        return mapToKonto(data.recipientName, data.recipientAccountId, data.recipientBankCode)
    }

    open fun mapToKonto(accountHolderName: String, iban: String, bic: String): Konto {
        val konto = Konto()

        konto.name = accountHolderName
        konto.iban = iban
        konto.bic = bic

        return konto
    }


    open fun mapAccounts(bank: TypedBankData, accounts: Array<out Konto>, passport: HBCIPassport): List<TypedBankAccount> {
        return accounts.map { account ->
            val iban = if (account.iban.isNullOrBlank() == false) account.iban else passport.upd.getProperty("KInfo.iban") ?: ""

            val result = modelCreator.createAccount(bank, account.number,
                if (account.name2.isNullOrBlank() == false) account.name + " " + account.name2 else account.name)

            result.iban = iban
            result.subAccountNumber = account.subnumber
            result.customerId = account.customerid

            result.currency = account.curr
            result.type = mapBankAccountType(account)
            result.isAccountTypeSupported = result.type == BankAccountType.Girokonto || result.type == BankAccountType.Festgeldkonto
            result.accountLimit = account.limit?.value?.let { mapValue(it).toString() }

            result.supportsRetrievingBalance = account.allowedGVs.contains("HKSAL")
            result.supportsRetrievingAccountTransactions = account.allowedGVs.contains("HKKAZ")
            result.supportsRetrievingBalance = account.allowedGVs.contains("HKCCS")

            result
        }
    }

    open fun mapBankAccountType(account: Konto): BankAccountType {
        val type = account.acctype

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


    open fun mapTanMethods(tanMethodsString: String): List<net.dankito.banking.ui.model.tan.TanMethod> {
        return tanMethodsString.split('|')
            .map { mapTanMethod(it) }
            .filterNotNull()
    }

    open fun mapTanMethod(tanMethodString: String): net.dankito.banking.ui.model.tan.TanMethod? {
        val parts = tanMethodString.split(':')

        if (parts.size > 1) {
            val code = parts[0]
            val displayName = parts[1]
            val displayNameLowerCase = displayName.toLowerCase()

            return when {
                // TODO: implement all TAN methods
                displayNameLowerCase.contains("chiptan") -> {
                    if (displayNameLowerCase.contains("qr")) {
                        modelCreator.createTanMethod(displayName, TanMethodType.ChipTanQrCode, code)
                    }
                    else {
                        modelCreator.createTanMethod(displayName, TanMethodType.ChipTanFlickercode, code)
                    }
                }

                displayNameLowerCase.contains("sms") -> modelCreator.createTanMethod(displayName, TanMethodType.SmsTan, code)
                displayNameLowerCase.contains("push") -> modelCreator.createTanMethod(displayName, TanMethodType.AppTan, code)

                // we filter out iTAN and Einschritt-Verfahren as they are not permitted anymore according to PSD2
                else -> null
            }
        }

        return null
    }

}
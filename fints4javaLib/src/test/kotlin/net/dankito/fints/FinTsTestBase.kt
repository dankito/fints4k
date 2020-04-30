package net.dankito.fints

import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Datum
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.fints.model.*
import net.dankito.fints.response.segments.AccountType
import net.dankito.fints.response.segments.ChangeTanMediaParameters
import net.dankito.fints.response.segments.JobParameters
import java.math.BigDecimal
import java.util.*


abstract class FinTsTestBase {

    companion object {
        const val BankCode = "12345678"

        const val BankCountryCode = Laenderkennzeichen.Germany

        const val BankFinTsServerAddress = "banking.supi-dupi-bank.de/fints30"

        val Bank = BankData(BankCode, BankCountryCode, "", "")

        const val CustomerId = "0987654321"

        const val Pin = "12345"

        const val Iban = "DE11$BankCode$CustomerId"

        const val Bic = "ABCDDEMM123"

        val Language = Dialogsprache.German

        val SecurityFunction = Sicherheitsfunktion.PIN_TAN_910

        const val ControlReference = "4477"

        val Customer = CustomerData(CustomerId, Pin, selectedTanProcedure = TanProcedure("chipTAN-optisch", SecurityFunction, TanProcedureType.ChipTanFlickercode), selectedLanguage = Language)

        val Currency = "EUR"

        val AccountHolderName = "Martina Musterfrau"

        val Account = AccountData(CustomerId, null, BankCountryCode, BankCode, Iban, CustomerId, AccountType.Girokonto, Currency, AccountHolderName, null, null, listOf(), listOf())

        const val ProductName = "FinTS-TestClient25Stellen"

        const val ProductVersion = "1"

        val Product = ProductData(ProductName, ProductVersion)

        const val Date = 19880327

        const val Time = 182752


        init {
            Bank.changeTanMediumParameters = ChangeTanMediaParameters(JobParameters("", 1, 1, 1, ":0:0"), false, false, false, false, false, listOf())
        }
    }


    protected open fun createDialogId(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    protected open fun convertAmount(amount: BigDecimal): String {
        return amount.toString().replace('.', ',')
    }

    protected open fun convertDate(date: Date): String {
        return Datum.HbciDateFormat.format(date)
    }

    protected open fun unmaskString(string: String): String {
        return string.replace("?'", "'").replace("?+", "+").replace("?:", ":")
    }

    protected open fun normalizeBinaryData(message: String): String {
        return message.replace(0.toChar(), ' ')
    }


    protected open fun createEmptyJobParameters(): JobParameters {
        return JobParameters("", 1, 1, 1, ":0:0")
    }

}
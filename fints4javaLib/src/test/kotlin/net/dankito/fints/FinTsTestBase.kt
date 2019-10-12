package net.dankito.fints

import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Datum
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.fints.model.*
import java.math.BigDecimal
import java.util.*


abstract class FinTsTestBase {

    companion object {
        const val BankCode = "12345678"

        val Bank = BankData(BankCode, Laenderkennzeichen.Germany, "")

        const val CustomerId = "0987654321"

        const val Pin = "12345"

        val Language = Dialogsprache.German

        val SecurityFunction = Sicherheitsfunktion.PIN_TAN_911

        const val ControlReference = "1"

        val Customer = CustomerData(CustomerId, Pin, selectedTanProcedure = TanProcedure("chipTAN-optisch", SecurityFunction, TanProcedureType.ChipTan), selectedLanguage = Language)

        const val ProductName = "FinTS-TestClient25Stellen"

        const val ProductVersion = "1"

        val Product = ProductData(ProductName, ProductVersion)

        const val Date = 19880327

        const val Time = 182752
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

    protected open fun normalizeBinaryData(message: String): String {
        return message.replace(0.toChar(), ' ')
    }

}
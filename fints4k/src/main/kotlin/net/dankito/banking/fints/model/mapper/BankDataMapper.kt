package net.dankito.banking.fints.model.mapper

import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.banking.fints.model.BankData
import net.dankito.banking.fints.model.BankInfo


open class BankDataMapper {

    open fun mapFromBankInfo(bankInfo: BankInfo): BankData {
        return BankData(
            getBankCodeForOnlineBanking(bankInfo),
            Laenderkennzeichen.Germany, // TODO: currently there are only German banks. But change this if ever other countries get supported
            bankInfo.pinTanAddress ?: "",
            name = bankInfo.name,
            bic = bankInfo.bic
        )
    }

    protected open fun getBankCodeForOnlineBanking(bankInfo: BankInfo): String {
        // for UniCredit / HypVereinsbank for online banking '70020270' has to be used as bank code
        if (bankInfo.name.contains("unicredit", true)) {
            return "70020270"
        }

        return bankInfo.bankCode
    }

}
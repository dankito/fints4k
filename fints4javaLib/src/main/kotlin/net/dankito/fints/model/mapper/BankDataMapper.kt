package net.dankito.fints.model.mapper

import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Laenderkennzeichen
import net.dankito.fints.model.BankData
import net.dankito.fints.model.BankInfo


open class BankDataMapper {

    open fun mapFromBankInfo(bankInfo: BankInfo): BankData {
        return BankData(
            bankInfo.bankCode,
            Laenderkennzeichen.Germany, // TODO: currently there are only German banks. But change this if ever other countries get supported
            bankInfo.pinTanAddress ?: "",
            name = bankInfo.name,
            bic = bankInfo.bic
        )
    }

}
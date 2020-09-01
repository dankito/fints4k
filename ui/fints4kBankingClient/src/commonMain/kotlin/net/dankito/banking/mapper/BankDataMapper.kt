package net.dankito.banking.mapper

import net.dankito.banking.fints.model.BankData
import net.dankito.banking.bankfinder.BankInfo


open class BankDataMapper {

    open fun mapFromBankInfo(bankInfo: BankInfo): BankData {
        return BankData(
            bankInfo.bankCode,
            bankInfo.pinTanAddress ?: "",
            bankInfo.bic,
            bankInfo.name
        )
    }

}
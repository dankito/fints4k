package net.dankito.banking.fints.rest.model.dto.response

import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMedium


open class BankResponseDto(
    open val bankCode: String,
    open val userName: String,
    open val finTs3ServerAddress: String,
    open val bic: String,

    open val bankName: String,

    open val userId: String,
    open val customerName: String,

    open val usersTanMethods: List<TanMethodResponseDto>,
    open val selectedTanMethod: TanMethodResponseDto?,
    open val tanMedia: List<TanMedium>,
    
    open val supportedHbciVersions: List<String>
)
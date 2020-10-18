package net.dankito.banking.fints.rest.model.dto.response

import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.AllowedTanFormat
import net.dankito.banking.fints.model.TanMethodType


open class TanMethodResponseDto(
    open val displayName: String,
    open val bankInternalMethodCode: String,
    open val type: TanMethodType,
    open val hhdVersion: String? = null,
    open val maxTanInputLength: Int? = null,
    open val allowedTanFormat: AllowedTanFormat = AllowedTanFormat.Alphanumeric,
    open val nameOfTanMediumRequired: Boolean = false
)
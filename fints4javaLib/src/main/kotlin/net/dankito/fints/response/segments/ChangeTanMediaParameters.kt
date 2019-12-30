package net.dankito.fints.response.segments


open class ChangeTanMediaParameters(
    parameters: JobParameters,
    val enteringTanListNumberRequired: Boolean,
    val enteringCardSequenceNumberRequired: Boolean,
    val enteringAtcAndTanRequired: Boolean,
    val enteringCardTypeAllowed: Boolean,
    val accountInfoRequired: Boolean,
    val allowedCardTypes: List<Int>
)
    : JobParameters(parameters)
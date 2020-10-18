package net.dankito.banking.util


open class ValidationResult(
    open val inputString: String,
    open val validationSuccessful: Boolean,
    open val didCorrectString: Boolean = false,
    open val correctedInputString: String = inputString,
    open val validationError: String? = null,
    open val validationHint: String? = null
) {

    open val validationSuccessfulOrCouldCorrectString: Boolean = validationSuccessful || didCorrectString

}
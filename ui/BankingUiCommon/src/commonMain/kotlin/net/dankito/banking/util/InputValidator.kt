package net.dankito.banking.util

import net.dankito.banking.fints.messages.segmente.implementierte.sepa.ISepaMessageCreator
import net.dankito.banking.fints.messages.segmente.implementierte.sepa.SepaMessageCreator
import net.dankito.utils.multiplatform.BigDecimal


open class InputValidator {

    companion object {

        const val RecipientNameMaxLength = 70

        const val IbanMaxLength = 34

        const val BicMaxLength = 11

        const val ReferenceMaxLength = 140

        const val MinimumLengthToDetermineBicFromIban = 12 // TODO: this is only true for German (and may some other) IBANs


        /**
         * The IBAN consists of up to 34 alphanumeric characters, as follows:
         * - country code using ISO 3166-1 alpha-2 – two letters,
         * - check digits – two digits, and
         * - Basic Bank Account Number (BBAN) – up to 30 alphanumeric characters that are country-specific.
         * (https://en.wikipedia.org/wiki/International_Bank_Account_Number#Structure)
         */
        const val IbanPatternString = "[A-Z]{2}\\d{2}[A-Z0-9]{10,30}"
        val IbanPattern = Regex("^" + IbanPatternString + "\$")

        /**
         * The IBAN should not contain spaces when transmitted electronically. When printed it is expressed in groups
         * of four characters separated by a single space, the last group being of variable length as shown in the example below
         * (https://en.wikipedia.org/wiki/International_Bank_Account_Number#Structure)
         */
        const val IbanWithSpacesPatternString = "[A-Z]{2}\\d{2}\\s([A-Z0-9]{4}\\s){3}[A-Z0-9\\s]{1,18}"
        val IbanWithSpacesPattern = Regex("^" + IbanWithSpacesPatternString + "\$")

        val InvalidIbanCharactersPattern = Regex("[^A-Z0-9 ]")


        /**
         * The SWIFT code is 8 or 11 characters, made up of:
         * - 4 letters: institution code or bank code.
         * - 2 letters: ISO 3166-1 alpha-2 country code (exceptionally, SWIFT has assigned the code XK to Republic of Kosovo, which does not have an ISO 3166-1 country code)
         * - 2 letters or digits: location code
         * -- if the second character is "0", then it is typically a test BIC as opposed to a BIC used on the live network.
         * -- if the second character is "1", then it denotes a passive participant in the SWIFT network
         * -- if the second character is "2", then it typically indicates a reverse billing BIC, where the recipient pays for the message as opposed to the more usual mode whereby the sender pays for the message.
         * - 3 letters or digits: branch code, optional ('XXX' for primary office)
         * Where an eight digit code is given, it may be assumed that it refers to the primary office.
         */
        const val BicPatternString = "[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}(?:\\b|[A-Z0-9]{03})"
        val BicPattern = Regex("^" + BicPatternString + "$")

        val InvalidBicCharactersPattern = Regex("[^A-Z0-9]")


        val InvalidSepaCharactersPattern = Regex("[^${SepaMessageCreator.AllowedSepaCharacters}]+")
    }


    protected val sepaMessageCreator: ISepaMessageCreator = SepaMessageCreator()


    open fun validateRecipientNameWhileTyping(recipientNameToTest: String): ValidationResult {
        return validateRecipientName(recipientNameToTest, true)
    }

    open fun validateRecipientName(recipientNameToTest: String): ValidationResult {
        return validateRecipientName(recipientNameToTest, false)
    }

    open fun validateRecipientName(recipientNameToTest: String, userIsStillTyping: Boolean = false): ValidationResult {
        if (isRecipientNameValid(recipientNameToTest)) {
            return ValidationResult(recipientNameToTest, true)
        }

        if (recipientNameToTest.isEmpty()) {
            if (userIsStillTyping) { // if user is still typing, don't check if something has been entered yet
                return ValidationResult(recipientNameToTest, true)
            }
            return ValidationResult(recipientNameToTest, false, validationError = "Bitte geben Sie den Namen des Empfängers ein") // TODO: translate
        }

        if (hasRecipientNameValidLength(recipientNameToTest) == false) {
            val correctedString = recipientNameToTest.substring(0, RecipientNameMaxLength)
            return ValidationResult(recipientNameToTest, isRecipientNameValid(correctedString), true, correctedString, "Name darf maximal 70 Zeichen lang sein") // TODO: translate
        }


        val invalidRecipientNameCharacters = getInvalidSepaCharacters(recipientNameToTest)

        val correctedString = getCorrectedString(recipientNameToTest, invalidRecipientNameCharacters, true)
        return ValidationResult(recipientNameToTest, isRecipientNameValid(correctedString), true, correctedString, null, "Unzulässige(s) Zeichen eingegeben: $invalidRecipientNameCharacters") // TODO: translate
    }

    open fun isRecipientNameValid(stringToTest: String): Boolean {
        return hasRecipientNameValidLength(stringToTest)
                && containsOnlyValidSepaCharacters(stringToTest)
    }

    open fun hasRecipientNameValidLength(stringToTest: String): Boolean {
        return stringToTest.length in 1..RecipientNameMaxLength
    }


    /**
     * Validate entered IBAN while user is still typing. Just checks (and corrects) if invalid
     * characters have been entered or string is too long.
     *
     * Doesn't check yet if entered text has the correct pattern, min length etc. as user may is
     * just about to enter this information.
     */
    open fun validateIbanWhileTyping(ibanToTest: String): ValidationResult {
        return validateIban(ibanToTest, true)
    }

    open fun validateIban(ibanToTest: String): ValidationResult {
        return validateIban(ibanToTest, false)
    }

    protected open fun validateIban(ibanToTest: String, userIsStillTyping: Boolean = false): ValidationResult {
        if (isValidIban(ibanToTest)) {
            return ValidationResult(ibanToTest, true)
        }

        if (ibanToTest.isBlank()) {
            if (userIsStillTyping) { // if user is still typing, don't check if something has been entered yet
                return ValidationResult(ibanToTest, true)
            }
            return ValidationResult(ibanToTest, false, validationError = "Bitte geben Sie die IBAN des Empfängers ein") // TODO: translate
        }

        if (ibanToTest.length > IbanMaxLength) {
            val correctedString = ibanToTest.substring(0, IbanMaxLength)
            return ValidationResult(ibanToTest, isValidIban(correctedString), true, correctedString, null, "Eine IBAN darf maximal 34 Zeichen lang sein") // TODO: translate // TODO: may test country specific IBAN length, e.g. German IBANs have 22 charactersaa
        }

        val invalidIbanCharacters = getInvalidIbanCharacters(ibanToTest)

        if (invalidIbanCharacters.isNotEmpty()) {
            val correctedString = getCorrectedString(ibanToTest, invalidIbanCharacters)
            return ValidationResult(ibanToTest, isValidIban(correctedString), true, correctedString, null, "Unzulässige(s) Zeichen eingegeben: $invalidIbanCharacters") // TODO: translate
        }
        else if (userIsStillTyping) { // entered IBAN hasn't required pattern yet but that's ok as user is may just about to provide that information
            return ValidationResult(ibanToTest, true)
        }
        else {
            return ValidationResult(ibanToTest, false, validationError = "IBANs haben folgendes Muster: DE12 1234 5678 9012 3456 78") // TODO: translate
        }
    }

    open fun isValidIban(stringToTest: String): Boolean {
        return IbanPattern.matches(stringToTest.replace(" ", ""))
    }

    open fun getInvalidIbanCharacters(string: String): String {
        return getInvalidCharacters(string, InvalidIbanCharactersPattern)
    }


    open fun validateBic(bicToTest: String): ValidationResult {
        if (isValidBic(bicToTest)) {
            return ValidationResult(bicToTest, true)
        }
        else {
            if (bicToTest.isBlank()) {
                return ValidationResult(bicToTest, false, validationError = "Bitte geben Sie die BIC des Empfängers ein") // TODO: translate
            }
            else if (bicToTest.length > BicMaxLength) {
                val correctedString = bicToTest.substring(0, BicMaxLength)
                return ValidationResult(bicToTest, isValidBic(correctedString), true, correctedString, null, "Eine IBAN darf maximal 11 Zeichen lang sein") // TODO: translate // TODO: may test country specific IBAN length, e.g. German IBANs have 22 charactersaa
            }
            else {
                val invalidBicCharacters = getInvalidBicCharacters(bicToTest)
                if (invalidBicCharacters.isNotEmpty()) {
                    val correctedString = getCorrectedString(bicToTest, invalidBicCharacters)
                    return ValidationResult(bicToTest, isValidBic(correctedString), true, correctedString, null, "Unzulässige(s) Zeichen eingegeben: $invalidBicCharacters") // TODO: translate
                }
                else {
                    return ValidationResult(bicToTest, false, validationError = "Eine BIC besteht aus 8 oder 11 Zeichen und folgt dem Muster: ABCDED12(XYZ)") // TODO: translate
                }
            }
        }
    }

    open fun isValidBic(stringToTest: String): Boolean {
        return BicPattern.matches(stringToTest)
    }

    open fun getInvalidBicCharacters(string: String): String {
        return getInvalidCharacters(string, InvalidBicCharactersPattern)
    }


    open fun validateAmount(enteredAmountString: String): ValidationResult {
        if (enteredAmountString.isBlank()) {
            return ValidationResult(enteredAmountString, false, validationError = "Bitte geben Sie den zu überweisenden Betrag ein") // TODO: translate
        }

        convertAmountString(enteredAmountString)?.let { amount ->
            if (amount.isPositive && amount != BigDecimal.Zero) {
                return ValidationResult(enteredAmountString, true)
            }
        }

        return ValidationResult(enteredAmountString, false, validationError = "Bitte geben Sie einen Betrag größer 0 ein.") // TODO: translate
    }

    open fun convertAmountString(enteredAmountString: String): BigDecimal? {
        try {
            val amountString = enteredAmountString.replace(',', '.')

            return BigDecimal(amountString)
        } catch (ignored: Exception) { }

        return null
    }


    open fun validateReference(referenceToTest: String): ValidationResult {
        if (isReferenceValid(referenceToTest)) {
            return ValidationResult(referenceToTest, true)
        }

        if (hasReferenceValidLength(referenceToTest) == false) {
            val correctedString = referenceToTest.substring(0, ReferenceMaxLength)
            return ValidationResult(referenceToTest, isReferenceValid(correctedString), true, correctedString, "Verwendungszweck darf nur 140 Zeichen lang sein") // TODO: translate
        }


        val invalidReferenceCharacters = getInvalidSepaCharacters(referenceToTest)
        val correctedString = getCorrectedString(referenceToTest, invalidReferenceCharacters, true)
        return ValidationResult(referenceToTest, isReferenceValid(correctedString), true, correctedString, null, "Unzulässige(s) Zeichen eingegeben: $invalidReferenceCharacters") // TODO: translate return ValidationResult(recipentNameToTest, false, validationError = "Unzulässige(s) Zeichen eingegeben: ") // TODO: translate
    }

    open fun isReferenceValid(stringToTest: String): Boolean {
        return hasReferenceValidLength(stringToTest)
                && containsOnlyValidSepaCharacters(stringToTest)
    }

    open fun hasReferenceValidLength(stringToTest: String): Boolean {
        return stringToTest.length in 0..ReferenceMaxLength // reference is not a required field -> may be empty
    }


    open fun containsOnlyValidSepaCharacters(stringToTest: String): Boolean {
        return sepaMessageCreator.containsOnlyAllowedCharacters(stringToTest)
    }

    open fun getInvalidSepaCharacters(string: String): String {
        return getInvalidCharacters(string, InvalidSepaCharactersPattern)
    }

    open fun convertToAllowedSepaCharacters(string: String): String {
        return sepaMessageCreator.convertDiacriticsAndReservedXmlCharacters(string)
    }


    open fun getInvalidCharacters(string: String, pattern: Regex): String {
        return pattern.findAll(string).map { it.value }.joinToString("")
    }

    // TODO: do not convert XML entities in user's. User will a) not understand what happened and b) afterwards auto correction will not work anymore (i think the issue lies in used Regex: '(&\w{2,4};)').
    // But take converted XML entities length into account when checking if recipient's name and reference length isn't too long
    protected open fun getCorrectedString(inputString: String, invalidCharacters: String, convertToAllowedSepaCharacters: Boolean = false): String {
        var correctedString = if (convertToAllowedSepaCharacters) convertToAllowedSepaCharacters(inputString) else inputString

        invalidCharacters.forEach { invalidChar ->
            correctedString = correctedString.replace(invalidChar.toString(), "")
        }

        return correctedString
    }

}
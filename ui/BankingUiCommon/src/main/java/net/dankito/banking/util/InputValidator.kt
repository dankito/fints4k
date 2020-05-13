package net.dankito.banking.util

import net.dankito.fints.messages.segmente.implementierte.sepa.ISepaMessageCreator
import net.dankito.fints.messages.segmente.implementierte.sepa.SepaMessageCreator
import java.util.regex.Pattern


open class InputValidator {

    companion object {

        /**
         * The IBAN consists of up to 34 alphanumeric characters, as follows:
         * - country code using ISO 3166-1 alpha-2 – two letters,
         * - check digits – two digits, and
         * - Basic Bank Account Number (BBAN) – up to 30 alphanumeric characters that are country-specific.
         * (https://en.wikipedia.org/wiki/International_Bank_Account_Number#Structure)
         */
        const val IbanPatternString = "[A-Z]{2}\\d{2}[A-Z0-9]{10,30}"
        val IbanPattern = Pattern.compile("^" + IbanPatternString + "\$")

        /**
         * The IBAN should not contain spaces when transmitted electronically. When printed it is expressed in groups
         * of four characters separated by a single space, the last group being of variable length as shown in the example below
         * (https://en.wikipedia.org/wiki/International_Bank_Account_Number#Structure)
         */
        const val IbanWithSpacesPatternString = "[A-Z]{2}\\d{2}\\s([A-Z0-9]{4}\\s){3}[A-Z0-9\\s]{1,18}"
        val IbanWithSpacesPattern = Pattern.compile("^" + IbanWithSpacesPatternString + "\$")

        val InvalidIbanCharactersPattern = Pattern.compile("[^A-Z0-9 ]")


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
        val BicPattern = Pattern.compile("^" + BicPatternString + "$")

        val InvalidBicCharactersPattern = Pattern.compile("[^A-Z0-9]")


        val InvalidSepaCharactersPattern = Pattern.compile("[^${SepaMessageCreator.AllowedSepaCharacters}]+")
    }


    protected val sepaMessageCreator: ISepaMessageCreator = SepaMessageCreator()


    open fun isValidIban(stringToTest: String): Boolean {
        return IbanPattern.matcher(stringToTest).matches() ||
                IbanWithSpacesPattern.matcher(stringToTest).matches()
    }

    open fun getInvalidIbanCharacters(string: String): String {
        return getInvalidCharacters(string, InvalidIbanCharactersPattern)
    }


    open fun isValidBic(stringToTest: String): Boolean {
        return BicPattern.matcher(stringToTest).matches()
    }

    open fun getInvalidBicCharacters(string: String): String {
        return getInvalidCharacters(string, InvalidBicCharactersPattern)
    }


    open fun containsOnlyValidSepaCharacters(stringToTest: String): Boolean {
        return sepaMessageCreator.containsOnlyAllowedCharacters(stringToTest)
    }

    open fun getInvalidSepaCharacters(string: String): String {
        return getInvalidCharacters(convertToAllowedSepaCharacters(string), InvalidSepaCharactersPattern)
    }

    open fun convertToAllowedSepaCharacters(string: String): String {
        return sepaMessageCreator.convertToAllowedCharacters(string)
    }


    open fun getInvalidCharacters(string: String, pattern: Pattern): String {
        val illegalCharacters = mutableSetOf<String>()

        val matcher = pattern.matcher(string)

        while (matcher.find()) {
            illegalCharacters.add(matcher.group())
        }

        return illegalCharacters.joinToString("")
    }

}
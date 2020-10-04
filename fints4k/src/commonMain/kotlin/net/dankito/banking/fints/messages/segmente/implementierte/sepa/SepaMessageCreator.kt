package net.dankito.banking.fints.messages.segmente.implementierte.sepa

import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.DateFormatter


/**
 * It may sounds like beginners programming loading a XML file and doing string replacements to set actual values.
 * And yes I know how to use xjc :).
 *
 * But there's some reason behind it:
 * - Serializing to XML is always a bit problematic on Android.
 * - I don't need another dependency.
 * - And it should be a little bit faster (even though not much :) ).
 */
open class SepaMessageCreator : ISepaMessageCreator {

    companion object {
        const val AllowedSepaCharacters = "A-Za-z0-9\\?,\\-\\+\\.,:/\\(\\)\'\" (&\\w{2,4};)"

        const val ReservedXmlCharacters = "\'\"&<>"

        val AllowedSepaCharactersPattern = Regex("^[$AllowedSepaCharacters]*$")

        val AllowedSepaCharactersExceptReservedXmlCharactersPattern = Regex("^[$AllowedSepaCharacters$ReservedXmlCharacters]*$")

        const val MessageIdKey = "MessageId"

        const val CreationDateTimeKey = "CreationDateTime"

        const val PaymentInformationIdKey = "PaymentInformationId"

        const val NumberOfTransactionsKey = "NumberOfTransactions"

        val IsoDateFormat = DateFormatter("yyyy-MM-dd'T'HH:mm:ss.SSS")
    }


    override fun containsOnlyAllowedCharacters(stringToTest: String): Boolean {
        return AllowedSepaCharactersPattern.matches(stringToTest)
                && convertDiacriticsAndReservedXmlCharacters(stringToTest) == stringToTest
    }

    override fun containsOnlyAllowedCharactersExceptReservedXmlCharacters(stringToTest: String): Boolean {
        return AllowedSepaCharactersExceptReservedXmlCharactersPattern.matches(stringToTest)
                && convertDiacritics(stringToTest) == stringToTest
    }

    override fun convertReservedXmlCharacters(input: String): String {
        // TODO: add other replacement strings
        return input
            .replace("\"", "&quot;")
            .replace("\'", "&apos;")
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }

    override fun convertDiacritics(input: String): String {
        return input

            .replace("Á", "A")
            .replace("À", "A")
            .replace("Â", "A")
            .replace("Ã", "A")
            .replace("Ä", "A")
            .replace("Å", "A")

            .replace("á", "a")
            .replace("à", "a")
            .replace("â", "a")
            .replace("ã", "a")
            .replace("ä", "a")
            .replace("å", "a")

            .replace("É", "E")
            .replace("È", "E")
            .replace("Ê", "E")
            .replace("Ë", "E")

            .replace("é", "e")
            .replace("è", "e")
            .replace("ê", "e")
            .replace("ë", "e")

            .replace("Í", "I")
            .replace("Ì", "I")
            .replace("Î", "I")
            .replace("Ï", "I")

            .replace("í", "i")
            .replace("ì", "i")
            .replace("î", "i")
            .replace("ï", "i")

            .replace("Ó", "O")
            .replace("Ò", "O")
            .replace("Ô", "O")
            .replace("Õ", "O")
            .replace("Ö", "O")

            .replace("ó", "o")
            .replace("ò", "o")
            .replace("ô", "o")
            .replace("õ", "o")
            .replace("ö", "o")

            .replace("Ú", "U")
            .replace("Ù", "U")
            .replace("Û", "U")
            .replace("Ü", "U")
            .replace("Ü", "U")

            .replace("ú", "u")
            .replace("ù", "u")
            .replace("û", "u")
            .replace("ũ", "u")
            .replace("ü", "u")

            .replace("Ç", "C")
            .replace("Č", "C")
            .replace("Ñ", "N")

            .replace("ç", "c")
            .replace("č", "c")
            .replace("ñ", "n")
            .replace("ß", "ss")
    }


    override fun createXmlFile(messageTemplate: PaymentInformationMessages, replacementStrings: Map<String, String>): String {
        var xmlFile = messageTemplate.xmlTemplate

        val now = Date()
        val nowInIsoDate = IsoDateFormat.format(now)

        if (replacementStrings.containsKey(MessageIdKey) == false) {
            xmlFile = replacePlaceholderWithValue(xmlFile, MessageIdKey, nowInIsoDate)
        }
        if (replacementStrings.containsKey(CreationDateTimeKey) == false) {
            xmlFile = replacePlaceholderWithValue(xmlFile, CreationDateTimeKey, nowInIsoDate)
        }
        if (replacementStrings.containsKey(PaymentInformationIdKey) == false) {
            xmlFile = replacePlaceholderWithValue(xmlFile, PaymentInformationIdKey, nowInIsoDate)
        }

        replacementStrings.forEach { entry ->
            xmlFile = replacePlaceholderWithValue(xmlFile, entry.key, entry.value)
        }

        return xmlFile
    }

    protected open fun replacePlaceholderWithValue(xmlFile: String, key: String, value: String): String {
        return xmlFile.replace("$$key$", value)
    }

}
package net.dankito.fints.messages.segmente.implementierte.sepa

import net.dankito.fints.messages.datenelemente.implementierte.sepa.SepaMessage
import org.slf4j.LoggerFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


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

        val AllowedSepaCharactersPattern: Pattern = Pattern.compile("^[$AllowedSepaCharacters]*$")

        const val MessageIdKey = "MessageId"

        const val CreationDateTimeKey = "CreationDateTime"

        const val PaymentInformationIdKey = "PaymentInformationId"

        const val NumberOfTransactionsKey = "NumberOfTransactions"

        val IsoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

        private val log = LoggerFactory.getLogger(SepaMessageCreator::class.java)
    }


    override fun containsOnlyAllowedCharacters(stringToTest: String): Boolean {
        val convertedString = convertToAllowedCharacters(stringToTest)

        return AllowedSepaCharactersPattern.matcher(convertedString).matches()
    }

    override fun convertToAllowedCharacters(input: String): String {
        // TODO: add other replacement strings
        return input
            .replace("\"", "&quot;")
            .replace("\'", "&apos;")
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

            // convert diacritics
            .replace("á", "a", true)
            .replace("à", "a", true)
            .replace("â", "a", true)
            .replace("ã", "a", true)
            .replace("ä", "a", true)
            .replace("å", "a", true)

            .replace("é", "e", true)
            .replace("è", "e", true)
            .replace("ê", "e", true)
            .replace("ë", "e", true)

            .replace("í", "i", true)
            .replace("ì", "i", true)
            .replace("î", "i", true)
            .replace("ï", "i", true)

            .replace("ó", "o", true)
            .replace("ò", "o", true)
            .replace("ô", "o", true)
            .replace("õ", "o", true)
            .replace("ö", "o", true)

            .replace("ú", "u", true)
            .replace("ù", "u", true)
            .replace("û", "u", true)
            .replace("ũ", "u", true)
            .replace("ü", "u", true)

            .replace("ç", "u", true)
            .replace("č", "u", true)
            .replace("ñ", "u", true)
            .replace("ß", "ss", true)
    }


    override fun createXmlFile(filename: String, replacementStrings: Map<String, String>): String {
        var xmlFile = loadXmlFile(filename)

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

    protected open fun loadXmlFile(filename: String): String {
        val filePath = "sepa/" + filename

        SepaMessage::class.java.classLoader.getResourceAsStream(filePath)?.use { inputStream ->
            return inputStream.bufferedReader().readText()
        }

        log.error("Could not load SEPA file from path ${File(filePath).absolutePath}") // TODO: how to inform user?

        return ""
    }

    protected open fun replacePlaceholderWithValue(xmlFile: String, key: String, value: String): String {
        return xmlFile.replace("$$key$", value)
    }

}
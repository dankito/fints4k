package net.dankito.banking.fints.messages.segmente.implementierte.sepa


interface ISepaMessageCreator {

    fun createXmlFile(messageTemplate: PaymentInformationMessages, replacementStrings: Map<String, String>): String

    fun containsOnlyAllowedCharacters(stringToTest: String): Boolean

    fun containsOnlyAllowedCharactersExceptReservedXmlCharacters(stringToTest: String): Boolean

    fun convertDiacriticsAndReservedXmlCharacters(input: String): String {
        var converted = convertDiacritics(input)

        return convertReservedXmlCharacters(converted)
    }

    fun convertReservedXmlCharacters(input: String): String

    fun convertDiacritics(input: String): String

}
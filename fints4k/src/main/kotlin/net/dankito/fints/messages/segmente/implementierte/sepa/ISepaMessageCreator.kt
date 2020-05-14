package net.dankito.fints.messages.segmente.implementierte.sepa


interface ISepaMessageCreator {

    fun createXmlFile(filename: String, replacementStrings: Map<String, String>): String

    fun convertDiacriticsAndReservedXmlCharactersAndCheckIfContainsOnlyAllowedCharacters(stringToTest: String): Boolean {
        val convertedString = convertDiacriticsAndReservedXmlCharacters(stringToTest)

        return containsOnlyAllowedCharacters(convertedString)
    }

    fun containsOnlyAllowedCharacters(stringToTest: String): Boolean

    fun convertDiacriticsAndReservedXmlCharacters(input: String): String

}
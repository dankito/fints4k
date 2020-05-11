package net.dankito.fints.messages.segmente.implementierte.sepa


interface ISepaMessageCreator {

    fun createXmlFile(filename: String, replacementStrings: Map<String, String>): String

    fun containsOnlyAllowedCharacters(stringToTest: String): Boolean

    fun convertToAllowedCharacters(input: String): String

}
package net.dankito.fints.messages.datenelemente.implementierte


inline fun <reified T : Enum<T>> allCodes(): List<String> {
    return enumValues<T>().map { (it as ICodeEnum).code }
}

interface ICodeEnum {

    val code: String

}
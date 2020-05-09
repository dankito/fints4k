package net.dankito.fints.messages.datenelemente.implementierte


enum class Kontoauszugsformat(override val code: String) : ICodeEnum {

    Mt940("1"),

    Iso8583("2"),

    DruckaufbereitetesDateiformat("3")

}
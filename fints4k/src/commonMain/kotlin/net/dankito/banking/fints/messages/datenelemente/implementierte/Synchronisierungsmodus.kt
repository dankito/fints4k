package net.dankito.banking.fints.messages.datenelemente.implementierte


enum class Synchronisierungsmodus(override val code: String) : ICodeEnum {

    NeueKundensystemIdZurueckmelden("0"),

    LetzteVerarbeiteteNachrichtennummerZurueckmelden("1"),

    SignaturIdZurueckmelden("2")

}
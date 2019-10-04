package net.dankito.fints.messages.datenelemente.implementierte.signatur


enum class VersionDesSicherheitsverfahrens(val methodNumber: Int) {

    PIN_Ein_Schritt(1),

    PIN_Zwei_Schritt(2),

    RAH_7(7),

    RAH_9(9),

    RAH_10(10)

}
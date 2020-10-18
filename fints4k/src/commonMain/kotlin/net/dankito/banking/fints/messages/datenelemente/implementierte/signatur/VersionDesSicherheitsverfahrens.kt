package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur


enum class VersionDesSicherheitsverfahrens(val methodNumber: Int) {

    /*
        PinTan:

        Version des Sicherheitsverfahrens
            „1“ : bei allen Nachrichten, wenn Dialog im Einschritt-Verfahren
            „2“ : bei allen Nachrichten, wenn Dialog im Zwei-Schritt-Verfahren

        Die Verwendung des Ein-Schritt-Verfahrens ist jedoch nur noch in bestimmten Situationen,
        z. B. zur Ermittlung der zugelassenen Sicherheitsverfahren, zugelassen.
     */

    Version_1(1),

    Version_2(2),

    Version_3(3),

    Version_4(4),

    Version_5(5),

    Version_6(6),

    Version_7(7),

    Version_8(8),

    Version_9(9),

    Version_10(10);


    companion object {

        val PinTanDefaultVersion = Version_2

    }

}
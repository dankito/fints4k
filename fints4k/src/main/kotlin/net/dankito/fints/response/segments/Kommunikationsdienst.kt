package net.dankito.fints.response.segments

import net.dankito.fints.messages.datenelemente.implementierte.ICodeEnum


/**
 * Unterstütztes Kommunikationsverfahren (Protokollstack).
Zur Zeit unterstützte Kommunikationsverfahren:
1: T-Online (mit FinTS V3.0 nicht mehr unterstützt)
2: TCP/IP (Protokollstack SLIP/PPP)
13: https (verwendet im Sicherheitsverfahren PIN/TAN)

 */
enum class Kommunikationsdienst(override val code: String) : ICodeEnum {

    T_Online("1"),

    TCP_IP("2"),

    Https("3")

}
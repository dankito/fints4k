package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.datenelemente.implementierte.ICodeEnum


enum class DatumUndZeitbezeichner(override val code: String) : ICodeEnum {

    Sicherheitszeitstempel("1"),

    CertificateRevocationTime("2")

}
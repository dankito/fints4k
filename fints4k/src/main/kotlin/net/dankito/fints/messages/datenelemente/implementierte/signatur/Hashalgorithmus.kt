package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.datenelemente.implementierte.ICodeEnum


enum class Hashalgorithmus(override val code: String) : ICodeEnum {

    SHA_256("3"),

    SHA_384("4"),

    SHA_512("5"),

    SHA_256_SHA_256("6"),

    Gegenseitig_vereinbart("999")

}
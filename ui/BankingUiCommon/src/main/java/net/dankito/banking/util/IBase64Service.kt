package net.dankito.banking.util

import java.nio.charset.Charset


interface IBase64Service {

    fun encode(text: String, charset: Charset): String

    fun decode(base64: String, charset: Charset): String

}
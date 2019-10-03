package net.dankito.fints.util

import java.nio.charset.Charset


interface IBase64Service {

    companion object {
        val DefaultCharset = Charsets.UTF_8
    }


    fun encode(text: String): String {
        return encode(text, DefaultCharset)
    }

    fun encode(text: String, charset: Charset): String


    fun decode(base64: String): String {
        return decode(base64, DefaultCharset)
    }

    fun decode(base64: String, charset: Charset): String

}
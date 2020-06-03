package net.dankito.banking.fints.util

import io.ktor.utils.io.charsets.Charset
import net.dankito.banking.fints.messages.HbciCharset


interface IBase64Service {

    companion object {
        val DefaultCharset = HbciCharset.DefaultCharset
    }


    fun encode(text: String, charset: Charset = DefaultCharset): String


    fun decode(base64: String, charset: Charset = DefaultCharset): String

}
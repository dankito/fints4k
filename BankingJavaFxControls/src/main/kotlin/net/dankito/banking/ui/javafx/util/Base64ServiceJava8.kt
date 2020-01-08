package net.dankito.banking.ui.javafx.util

import net.dankito.banking.util.IBase64Service
import java.nio.charset.Charset
import java.util.*


// TODO: use version from JavaFxUtils
open class Base64ServiceJava8 : IBase64Service {

    override fun encode(text: String, charset: Charset): String {
        return Base64.getEncoder().encodeToString(text.toByteArray(charset))
    }

    override fun decode(base64: String, charset: Charset): String {
        val decodedBytes = Base64.getDecoder().decode(base64)

        return String(decodedBytes, charset)
    }

}
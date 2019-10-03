package net.dankito.fints.util

import java.nio.charset.Charset
import java.util.*


// TODO: use version from JavaFxUtils
class Java8Base64Service : IBase64Service {


    override fun encode(text: String, charset: Charset): String {
        return Base64.getEncoder().encodeToString(text.toByteArray(charset))
    }

    override fun decode(base64: String, charset: Charset): String {
        val decodedBytes = Base64.getDecoder().decode(base64)

        return String(decodedBytes, charset)
    }

}

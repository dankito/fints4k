package net.dankito.banking.fints4java.android.util

import android.util.Base64
import net.dankito.banking.util.IBase64Service
import java.nio.charset.Charset


open class Base64ServiceAndroid : IBase64Service {

    override fun encode(text: String, charset: Charset): String {
        val bytes = text.toByteArray(charset)

        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    override fun decode(base64: String, charset: Charset): String {
        val decoded = Base64.decode(base64, Base64.NO_WRAP)

        return String(decoded, charset)
    }

}
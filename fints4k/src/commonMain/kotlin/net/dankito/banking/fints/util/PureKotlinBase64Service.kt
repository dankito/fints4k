package net.dankito.banking.fints.util

import io.ktor.utils.io.charsets.Charset


open class PureKotlinBase64Service : IBase64Service {

    protected val base64 = Base64()


    override fun encode(text: String, charset: Charset): String {
        return base64.encode(text)
    }

    override fun decode(base64: String, charset: Charset): String {
        return this.base64.decode(base64)
    }

}
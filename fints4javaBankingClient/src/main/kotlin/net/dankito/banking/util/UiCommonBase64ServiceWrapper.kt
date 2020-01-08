package net.dankito.banking.util

import java.nio.charset.Charset


open class UiCommonBase64ServiceWrapper(protected val base64Service: IBase64Service) : net.dankito.fints.util.IBase64Service {

    override fun encode(text: String, charset: Charset): String {
        return base64Service.encode(text, charset)
    }

    override fun decode(base64: String, charset: Charset): String {
        return base64Service.decode(base64, charset)
    }

}
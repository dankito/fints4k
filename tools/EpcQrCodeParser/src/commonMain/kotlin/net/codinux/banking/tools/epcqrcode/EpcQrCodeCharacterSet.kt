package net.codinux.banking.tools.epcqrcode


enum class EpcQrCodeCharacterSet(val code: Int) {

    UTF_8(1),

    ISO_8895_1(2),

    ISO_8895_2(3),

    ISO_8895_4(4),

    ISO_8895_5(5),

    ISO_8895_7(6),

    ISO_8895_10(7),

    ISO_8895_15(8)

}
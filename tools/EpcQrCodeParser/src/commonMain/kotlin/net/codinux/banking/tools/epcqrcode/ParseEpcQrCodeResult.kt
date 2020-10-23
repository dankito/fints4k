package net.codinux.banking.tools.epcqrcode


open class ParseEpcQrCodeResult(
    open val decodedQrCode: String,
    open val resultCode: ParseEpcQrCodeResultCode,
    open val epcQrCode: EpcQrCode?,
    open val error: String?
) {

    open val successful: Boolean
        get() = resultCode == ParseEpcQrCodeResultCode.Success


    override fun toString(): String {
        return if (successful) {
            "Success: $epcQrCode"
        }
        else {
            "Error: $error"
        }
    }

}
package net.dankito.fints.tan


open class TanImage(
    val mimeType: String,
    val imageBytes: ByteArray,
    val decodingError: Exception? = null
) {

    val decodingSuccessful: Boolean
        get() = decodingError == null


    override fun toString(): String {
        if (decodingSuccessful == false) {
            return "Decoding error: $decodingError"
        }

        return "$mimeType ${imageBytes.size} bytes"
    }

}
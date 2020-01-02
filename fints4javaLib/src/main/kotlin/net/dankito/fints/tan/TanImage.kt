package net.dankito.fints.tan


open class TanImage(
    val mimeType: String,
    val imageBytes: ByteArray,
    val error: Exception? = null
) {

    val decodingSuccessful: Boolean
        get() = error == null


    override fun toString(): String {
        if (decodingSuccessful == false) {
            return "Decoding error: $error"
        }

        return "$mimeType ${imageBytes.size} bytes"
    }

}
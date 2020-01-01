package net.dankito.fints.tan


open class TanImage(
    val mimeType: String,
    val imageBytes: ByteArray,
    val error: Exception? = null
) {

    val decodingSuccessful: Boolean
        get() = error == null


    override fun toString(): String {
        return "$mimeType ${imageBytes.size} bytes"
    }

}
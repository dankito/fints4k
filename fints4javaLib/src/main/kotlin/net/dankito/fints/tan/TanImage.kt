package net.dankito.fints.tan


open class TanImage(
    val mimeType: String,
    val imageBytes: ByteArray
) {

    override fun toString(): String {
        return "$mimeType ${imageBytes.size} bytes"
    }

}
package net.dankito.fints.tan


open class Flickercode(
    val challengeHHD_UC: String,
    val parsedDataSet: String,
    val error: Exception? = null
) {

    val decodingSuccessful: Boolean
        get() = error == null


    override fun toString(): String {
        if (decodingSuccessful == false) {
            return "Decoding error: $error"
        }

        return "Parsed $challengeHHD_UC to $parsedDataSet"
    }

}
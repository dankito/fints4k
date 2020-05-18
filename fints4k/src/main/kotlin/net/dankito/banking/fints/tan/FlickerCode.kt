package net.dankito.banking.fints.tan


open class FlickerCode(
    val challengeHHD_UC: String,
    val parsedDataSet: String,
    val decodingError: Exception? = null
) {

    val decodingSuccessful: Boolean
        get() = decodingError == null


    override fun toString(): String {
        if (decodingSuccessful == false) {
            return "Decoding error: $decodingError"
        }

        return "Parsed $challengeHHD_UC to $parsedDataSet"
    }

}
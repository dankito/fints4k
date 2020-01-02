package net.dankito.fints.tan


open class FlickerCodeDatenelement(
    val lengthInByte: String,
    val data: String,
    val encoding: FlickerCodeEncoding,
    val endIndex: Int
) {

    override fun toString(): String {
        return data
    }

}
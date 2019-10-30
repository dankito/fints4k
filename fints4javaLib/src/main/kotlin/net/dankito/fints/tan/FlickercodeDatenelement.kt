package net.dankito.fints.tan


open class FlickercodeDatenelement(
    val lengthInByte: String,
    val data: String,
    val encoding: FlickercodeEncoding,
    val endIndex: Int
) {

    override fun toString(): String {
        return data
    }

}
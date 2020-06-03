package net.dankito.banking.fints.tan


open class FlickerCodeDatenelement(
    val lengthInByte: String,
    val data: String,
    val encoding: FlickerCodeEncoding,
    val controlBytes: List<String>, // TODO: only available at start byte
    val endIndex: Int
) {

    override fun toString(): String {
        return data
    }

}
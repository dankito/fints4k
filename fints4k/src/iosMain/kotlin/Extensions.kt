import kotlinx.cinterop.*
import platform.Foundation.*


fun String.toNsData(): NSData {
    return this.encodeToByteArray().toNSData()
}

fun ByteArray.toNSData(): NSData = NSMutableData().apply {
    if (isEmpty()) return@apply
    this@toNSData.usePinned {
        appendBytes(it.addressOf(0), size.convert())
    }
}

fun NSData.toByteArray(): ByteArray {
    val data: CPointer<ByteVar> = bytes!!.reinterpret()

    return ByteArray(length.toInt()) { index -> data[index] }
}
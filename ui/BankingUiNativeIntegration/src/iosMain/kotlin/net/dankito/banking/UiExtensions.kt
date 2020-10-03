package net.dankito.banking

import kotlinx.cinterop.*
import platform.Foundation.*


/**
 * Swift doesn't see the ByteArray- and NSData extension methods from Common (why?) -> redefine them here in a way Swift sees them.
 */
class ByteArrayExtensions {

    companion object {

        fun toNSData(array: ByteArray): NSData {
            return NSMutableData().apply {
                if (array.isEmpty()) return@apply
                array.usePinned {
                    appendBytes(it.addressOf(0), array.size.convert())
                }
            }
        }

        fun fromNSData(data: NSData): ByteArray {
            val bytes: CPointer<ByteVar> = data.bytes!!.reinterpret()

            return ByteArray(data.length.toInt()) { index -> bytes[index] }
        }

    }
}
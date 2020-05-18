package net.dankito.banking.fints.tan

import net.dankito.banking.fints.messages.HbciCharset
import org.slf4j.LoggerFactory


open class TanImageDecoder {

    companion object {
        private val log = LoggerFactory.getLogger(TanImageDecoder::class.java)
    }


    open fun decodeChallenge(challengeHHD_UC: String): TanImage {
        try {
            val bytes = challengeHHD_UC.toByteArray(HbciCharset.DefaultCharset)

            val mimeTypeLength = getLength(bytes[0], bytes[1])
            val mimeTypeEnd = 2 + mimeTypeLength

            val mimeType = challengeHHD_UC.substring(2, mimeTypeEnd)

            val imageStart = mimeTypeEnd + 2

            // sometimes it happened that imageStart + getLength(bytes[mimeTypeEnd], bytes[mimeTypeEnd + 1])
            // was greater than challengeHHD_UC.length + 1 -> ignore image length and simply return all bytes starting with imageStart
            val imageBytes = bytes.copyOfRange(imageStart, bytes.size)

            return TanImage(mimeType, imageBytes)
        } catch (e: Exception) {
            log.error("Could not decode challenge HHD_UC to TanImage: $challengeHHD_UC", e)

            return TanImage("", ByteArray(0), e)
        }
    }

    protected open fun getLength(higherOrderByte: Byte, lowerOrderByte: Byte): Int {
        return 256 * byteToUnsignedInt(higherOrderByte) + byteToUnsignedInt(lowerOrderByte)
    }

    protected open fun byteToUnsignedInt(byte: Byte): Int {
        return byte.toUByte().toInt()
    }

}
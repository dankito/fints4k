package net.dankito.fints.tan

import net.dankito.fints.messages.HbciCharset
import org.slf4j.LoggerFactory


open class TanImageDecoder {

    companion object {
        private val log = LoggerFactory.getLogger(TanImageDecoder::class.java)
    }


    open fun decodeChallenge(challengeHHD_UC: String): TanImage? {
        try {
            val bytes = challengeHHD_UC.toByteArray(HbciCharset.DefaultCharset)

            val mimeTypeLength = getLength(bytes[0], bytes[1])
            val mimeTypeEnd = 2 + mimeTypeLength

            val mimeType = challengeHHD_UC.substring(2, mimeTypeEnd)

            val imageLength = getLength(bytes[mimeTypeEnd], bytes[mimeTypeEnd + 1])
            val imageStart = mimeTypeEnd + 2
            val imageString = challengeHHD_UC.substring(imageStart, imageStart + imageLength)

            val imageBytes = imageString.toByteArray(HbciCharset.DefaultCharset)

            return TanImage(mimeType, imageBytes)
        } catch (e: Exception) {
            log.error("Could not decode challenge HHD_UC to TanImage: $challengeHHD_UC", e)
        }

        return null
    }

    protected open fun getLength(higherOrderByte: Byte, lowerOrderByte: Byte): Int {
        return 256 * byteToUnsignedInt(higherOrderByte) + byteToUnsignedInt(lowerOrderByte)
    }

    protected open fun byteToUnsignedInt(byte: Byte): Int {
        return byte.toUByte().toInt()
    }

}
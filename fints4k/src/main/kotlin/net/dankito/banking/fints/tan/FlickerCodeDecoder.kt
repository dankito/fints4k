package net.dankito.banking.fints.tan

import net.dankito.banking.fints.model.HHDVersion
import org.slf4j.LoggerFactory
import java.util.regex.Pattern


open class FlickerCodeDecoder {

    companion object {
        val ContainsOtherSymbolsThanFiguresPattern: Pattern = Pattern.compile("\\D")

        private val log = LoggerFactory.getLogger(FlickerCodeDecoder::class.java)
    }


    open fun decodeChallenge(challengeHHD_UC: String, hhdVersion: HHDVersion): FlickerCode {
        try {
            val challengeLengthFieldLength = if (hhdVersion == HHDVersion.HHD_1_3) 2 else 3
            val challengeLength = parseIntToHex(challengeHHD_UC.substring(0, challengeLengthFieldLength))

            val startCode = parseStartCode(challengeHHD_UC, challengeLengthFieldLength, hhdVersion)

            val controlBytesString = startCode.controlBytes.joinToString("")

            val de1 = parseDatenelement(challengeHHD_UC, startCode.endIndex, hhdVersion)
            val de2 = parseDatenelement(challengeHHD_UC, de1.endIndex, hhdVersion)
            val de3 = parseDatenelement(challengeHHD_UC, de2.endIndex, hhdVersion)

            val luhnChecksum = calculateLuhnChecksum(startCode, controlBytesString, de1, de2, de3)

            // TODO:
            // können im HHDUC-Protokoll Datenelemente ausgelassen werden, indem als Länge LDE1, LDE2 oder LDE3 = ‘00‘ angegeben wird.
            // Dadurch wird gekennzeichnet, dass das jeweilige, durch den Start-Code definierte Datenelement nicht im HHDUC-Datenstrom
            // enthalten ist. Somit sind für leere Datenelemente die Längenfelder zu übertragen, wenn danach noch nicht-leere
            // Datenelemente folgen. Leere Datenelemente am Ende des Datenstromes können komplett inklusive Längenfeld entfallen.
            val dataWithoutLengthAndChecksum = startCode.lengthInByte + controlBytesString + startCode + de1.lengthInByte + de1.data + de2.lengthInByte + de2.data + de3.lengthInByte + de3.data
            val dataLength = (dataWithoutLengthAndChecksum.length + 2) / 2 // + 2 for checksum
            val dataWithoutChecksum = toHex(dataLength, 2) + dataWithoutLengthAndChecksum

            val xorChecksumString = calculateXorChecksum(dataWithoutChecksum)

            val parsedDataSet = dataWithoutChecksum + luhnChecksum + xorChecksumString

            return FlickerCode(challengeHHD_UC, parsedDataSet)
        } catch (e: Exception) {
            log.error("Could not decode challenge $challengeHHD_UC")

            return FlickerCode(challengeHHD_UC, "", e)
        }
    }

    protected open fun parseStartCode(challengeHHD_UC: String, startIndex: Int, hhdVersion: HHDVersion): FlickerCodeDatenelement {
        return parseDatenelement(challengeHHD_UC, startIndex, hhdVersion) { lengthByteString -> parseIntToHex(lengthByteString) }
    }

    protected open fun parseDatenelement(code: String, startIndex: Int, hhdVersion: HHDVersion): FlickerCodeDatenelement {
        return parseDatenelement(code, startIndex, hhdVersion) { lengthByteString -> lengthByteString.toInt() }
    }

    protected open fun parseDatenelement(code: String, startIndex: Int, hhdVersion: HHDVersion, lengthParser: (lengthByteString: String) -> Int): FlickerCodeDatenelement {
        val lengthByteLength = 2
        val dataElementAndRest = code.substring(startIndex)

        if (dataElementAndRest.isEmpty() || dataElementAndRest.length < lengthByteLength) { // data element not set
            return FlickerCodeDatenelement("", "", FlickerCodeEncoding.BCD, listOf(), startIndex)
        }

        val lengthByteString = dataElementAndRest.substring(0, lengthByteLength)
        val lengthByte = lengthParser(lengthByteString)

        var dataLength = getLengthFromLengthByte(lengthByte)
        var encoding = getEncodingFromLengthByte(lengthByte)

        val controlBytes = parseControlBytes(lengthByte, lengthByteLength, dataElementAndRest)

        val dataStartIndex = lengthByteLength + controlBytes.size * 2
        val dataEndIndex = dataStartIndex + dataLength
        var data = dataElementAndRest.substring(dataStartIndex, dataEndIndex)

        // Sollte ein Datenelement eine Zahl mit Komma-Trennung oder Vorzeichen beinhalten (z. B. Betrag oder Anzahl),
        // so muss als Format ASCII gewählt werden, da ggf. auch ein Sonderzeichen mit übertragen werden muss.
        if (ContainsOtherSymbolsThanFiguresPattern.matcher(data).find()) {
            encoding = FlickerCodeEncoding.ASCII
        }

        if (encoding == FlickerCodeEncoding.ASCII) {
            data = data.map { toHex(it.toInt(), 2) }.joinToString("")
        }

        if (encoding == FlickerCodeEncoding.BCD && data.length % 2 != 0) {
            data += "F" // Im Format BCD ggf. mit „F“ auf Bytegrenze ergänzt
        }

        dataLength = data.length

        val lengthInByteString = calculateLengthInByteString(dataLength, controlBytes, hhdVersion, encoding)

        return FlickerCodeDatenelement(
            lengthInByteString,
            data,
            encoding,
            controlBytes,
            startIndex + dataEndIndex
        )
    }

    protected open fun getLengthFromLengthByte(lengthByte: Int): Int {
        return lengthByte and 0b00011111
    }

    protected open fun getEncodingFromLengthByte(lengthByte: Int): FlickerCodeEncoding {
        return if (isBitSet(lengthByte, 6)) FlickerCodeEncoding.ASCII else FlickerCodeEncoding.BCD
    }

    protected open fun isControlBitSet(lengthByte: Int): Boolean {
        return isBitSet(lengthByte, 7)
    }


    protected open fun parseControlBytes(lengthByte: Int, lengthByteLength: Int, dataElementAndRest: String): MutableList<String> {
        val controlBytes = mutableListOf<String>()
        var isControlByteSet = isControlBitSet(lengthByte)

        while (isControlByteSet) {
            val controlByteStartIndex = lengthByteLength + controlBytes.size * 2
            val controlByteString = dataElementAndRest.substring(controlByteStartIndex, controlByteStartIndex + 2)
            val controlByte = parseIntToHex(controlByteString)

            controlBytes.add(controlByteString)

            isControlByteSet = isControlBitSet(controlByte)
        }

        return controlBytes
    }


    protected open fun calculateLengthInByteString(dataLength: Int, controlBytes: MutableList<String>,
                                                   hhdVersion: HHDVersion, encoding: FlickerCodeEncoding): String {

        var lengthInByte = dataLength / 2 + controlBytes.size * 128

        if (hhdVersion == HHDVersion.HHD_1_4 && encoding == FlickerCodeEncoding.ASCII) {
            lengthInByte += 64
        }

        if (encoding == FlickerCodeEncoding.ASCII) {
            if (lengthInByte < 16) {
                lengthInByte += 16 // set left half byte to '1' for ASCII
            }
        }

        return toHex(lengthInByte, 2)
    }


    protected open fun calculateLuhnChecksum(startCode: FlickerCodeDatenelement, controlBytes: String,
                                             de1: FlickerCodeDatenelement, de2: FlickerCodeDatenelement, de3: FlickerCodeDatenelement): Int {

        val luhnData = controlBytes + startCode.data + de1.data + de2.data + de3.data

        val luhnSum = luhnData.mapIndexed { index, char ->
            val asNumber = char.toString().toInt(16)

            if (index % 2 == 1) {
                val doubled = asNumber * 2
                return@mapIndexed (doubled / 10) + (doubled % 10)
            }

            asNumber
        }.sum()

        val luhnSumModulo10 = luhnSum % 10

        // Schritt 3: Das Ergebnis der Addition aus Schritt 2 ist von dem auf die nächst höhere Zahl mit der
        // Einerstelle 0 aufgerundeten Ergebnis der Addition aus Schritt 2 abzuziehen. Wenn das Ergebnis der Addition
        // aus Schritt 2 bereits eine Zahl mit der Einerstelle 0 ergibt (z. B. 30, 40, usw.), ist die Prüfziffer 0.
        if (luhnSumModulo10 == 0) {
            return 0
        }

        return 10 - (luhnSum % 10)
    }

    protected open fun calculateXorChecksum(dataWithoutChecksum: String): String {
        var xorChecksum = 0
        val xorByteData = dataWithoutChecksum.map { parseIntToHex(it) }

        xorByteData.forEach { xorChecksum = xorChecksum xor it }

        return toHex(xorChecksum, 1)
    }


    protected open fun toHex(number: Int, minLength: Int): String {
        var result = number.toString (16).toUpperCase()

        while (result.length < minLength) {
            result = '0' + result
        }

        return result
    }

    protected open fun parseIntToHex(char: Char): Int {
        return parseIntToHex(char.toString())
    }

    protected open fun parseIntToHex(string: String): Int {
        return Integer.parseInt(string, 16)
    }

    protected open fun isBitSet(num: Int, bit: Int): Boolean {
        return num and (1 shl bit) != 0
    }

}
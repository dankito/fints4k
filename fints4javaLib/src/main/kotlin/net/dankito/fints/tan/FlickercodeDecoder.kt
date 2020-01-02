package net.dankito.fints.tan

import org.slf4j.LoggerFactory
import java.util.regex.Pattern


open class FlickercodeDecoder {

    companion object {
        val ContainsOtherSymbolsThanFiguresPattern: Pattern = Pattern.compile("\\D")

        private val log = LoggerFactory.getLogger(FlickercodeDecoder::class.java)
    }


    open fun decodeChallenge(challengeHHD_UC: String): Flickercode {
        try {
            val challengeLength = parseIntToHex(challengeHHD_UC.substring(0, 2))

            val startCode = parseStartCode(challengeHHD_UC, 2)

            val controlByte = "" // TODO (there can be multiple of them!)

            val de1 = parseDatenelement(challengeHHD_UC, startCode.endIndex)
            val de2 = parseDatenelement(challengeHHD_UC, de1.endIndex)
            val de3 = parseDatenelement(challengeHHD_UC, de2.endIndex)

            val luhnChecksum = calculateLuhnChecksum(startCode, controlByte, de1, de2, de3)

            // TODO:
            // können im HHDUC-Protokoll Datenelemente ausgelassen werden, indem als Länge LDE1, LDE2 oder LDE3 = ‘00‘ angegeben wird.
            // Dadurch wird gekennzeichnet, dass das jeweilige, durch den Start-Code definierte Datenelement nicht im HHDUC-Datenstrom
            // enthalten ist. Somit sind für leere Datenelemente die Längenfelder zu übertragen, wenn danach noch nicht-leere
            // Datenelemente folgen. Leere Datenelemente am Ende des Datenstromes können komplett inklusive Längenfeld entfallen.
            val dataWithoutLengthAndChecksum = startCode.lengthInByte + controlByte + startCode + de1.lengthInByte + de1.data + de2.lengthInByte + de2.data + de3.lengthInByte + de3.data
            val dataLength = (dataWithoutLengthAndChecksum.length + 2) / 2 // + 2 for checksum
            val dataWithoutChecksum = toHex(dataLength, 2) + dataWithoutLengthAndChecksum

            val xorChecksumString = calculateXorChecksum(dataWithoutChecksum)

            val parsedDataSet = dataWithoutChecksum + luhnChecksum + xorChecksumString

            return Flickercode(challengeHHD_UC, parsedDataSet)
        } catch (e: Exception) {
            log.error("Could not decode challenge $challengeHHD_UC")

            return Flickercode(challengeHHD_UC, "", e)
        }
    }

    protected fun parseStartCode(challengeHHD_UC: String, startIndex: Int): FlickercodeDatenelement {
        return parseDatenelement(challengeHHD_UC, startIndex) { lengthByteString -> parseIntToHex(lengthByteString) }
    }

    protected open fun parseDatenelement(code: String, startIndex: Int): FlickercodeDatenelement {
        return parseDatenelement(code, startIndex) { lengthByteString -> lengthByteString.toInt() }
    }

    protected open fun parseDatenelement(code: String, startIndex: Int, lengthParser: (lengthByteString: String) -> Int): FlickercodeDatenelement {
        val lengthByteLength = 2
        val dataElementAndRest = code.substring(startIndex)

        if (dataElementAndRest.isEmpty() || dataElementAndRest.length < lengthByteLength) { // data element not set
            return FlickercodeDatenelement("", "", FlickercodeEncoding.BCD, startIndex)
        }

        val lengthByteString = dataElementAndRest.substring(0, lengthByteLength)
        val lengthByte = lengthParser(lengthByteString)

        var encoding = getEncodingFromLengthByte(lengthByte)
        var dataLength = getLengthFromLengthByte(lengthByte)

        val endIndex = lengthByteLength + dataLength
        var data = dataElementAndRest.substring(lengthByteLength, endIndex)

        // Sollte ein Datenelement eine Zahl mit Komma-Trennung oder Vorzeichen beinhalten (z. B. Betrag oder Anzahl),
        // so muss als Format ASCII gewählt werden, da ggf. auch ein Sonderzeichen mit übertragen werden muss.
        if (ContainsOtherSymbolsThanFiguresPattern.matcher(data).find()) {
            encoding = FlickercodeEncoding.ASCII
        }

        if (encoding == FlickercodeEncoding.ASCII) {
            data = data.map { toHex(it.toInt(), 2) }.joinToString("")
        }

        if (encoding == FlickercodeEncoding.BCD && data.length % 2 != 0) {
            data += "F" // Im Format BCD ggf. mit „F“ auf Bytegrenze ergänzt
        }

        dataLength = data.length

        var lengthInByte = dataLength / 2

        if (encoding == FlickercodeEncoding.ASCII) {
            if (lengthInByte < 16) {
                lengthInByte += 16 // set left half byte to '1' for ASCII
            }
        }

        val lengthInByteString = toHex(lengthInByte, 2)

        return FlickercodeDatenelement(
            lengthInByteString,
            data,
            encoding,
            startIndex + endIndex
        )
    }

    protected open fun getEncodingFromLengthByte(engthByte: Int): FlickercodeEncoding {
        return if (isBitSet(engthByte, 6)) FlickercodeEncoding.ASCII else FlickercodeEncoding.BCD
    }

    protected open fun getLengthFromLengthByte(lengthByte: Int): Int {
        return lengthByte and 0b00011111
    }


    protected open fun calculateLuhnChecksum(startCode: FlickercodeDatenelement, controlByte: String,
                                             de1: FlickercodeDatenelement, de2: FlickercodeDatenelement, de3: FlickercodeDatenelement): Int {

        val luhnData = controlByte + startCode.data + de1.data + de2.data + de3.data

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
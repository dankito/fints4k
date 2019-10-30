package net.dankito.fints.tan

import java.util.regex.Pattern


open class FlickercodeDecoder {

    companion object {
        val ContainsOtherSymbolsThanFiguresPattern: Pattern = Pattern.compile("\\D")
    }


    open fun decodeChallenge(challengeHHD_UC: String): Flickercode {
        val challengeLength = parseIntToHex(challengeHHD_UC.substring(0, 2))

        val startCodeLengthByte = parseIntToHex(challengeHHD_UC.substring(2, 4))
        val hasControlByte = isBitSet(startCodeLengthByte, 7)
        val startCodeEncoding = getEncodingFromLengthByte(startCodeLengthByte)
        val startCodeLength = getLengthFromLengthByte(startCodeLengthByte)

        val controlByte = "" // TODO (there can be multiple of them!)

        val startCodeStartIndex = if (hasControlByte) 6 else 4
        val startCodeEndIndex = startCodeStartIndex + startCodeLength
        var startCode = challengeHHD_UC.substring(startCodeStartIndex, startCodeEndIndex)
        if (startCode.length % 2 != 0) {
            startCode += "F" // Im Format BCD ggf. mit „F“ auf Bytegrenze ergänzt
        }

        val de1 = parseDatenelement(challengeHHD_UC, startCodeEndIndex)
        val de2 = parseDatenelement(challengeHHD_UC, de1.endIndex)
        val de3 = parseDatenelement(challengeHHD_UC, de2.endIndex)

        val luhnData = controlByte + startCode + de1.data + de2.data + de3.data

        val luhnSum = luhnData.mapIndexed { index, char ->
            val asNumber = char.toString().toInt(16)

            if (index % 2 == 1) {
                val doubled = asNumber * 2
                return@mapIndexed (doubled / 10) + (doubled % 10)
            }

            asNumber
        }.sum()

        val luhnChecksum = 10 - (luhnSum % 10)

        val countStartCodeBytes = startCodeLength / 2
        // TODO:
        // können im HHDUC-Protokoll Datenelemente ausgelassen werden, indem als Länge LDE1, LDE2 oder LDE3 = ‘00‘ angegeben wird.
        // Dadurch wird gekennzeichnet, dass das jeweilige, durch den Start-Code definierte Datenelement nicht im HHDUC-Datenstrom
        // enthalten ist. Somit sind für leere Datenelemente die Längenfelder zu übertragen, wenn danach noch nicht-leere
        // Datenelemente folgen. Leere Datenelemente am Ende des Datenstromes können komplett inklusive Längenfeld entfallen.
        val dataWithoutLengthAndChecksum = toHex(countStartCodeBytes, 2) + controlByte + startCode + de1.lengthInByte + de1.data + de2.lengthInByte + de2.data + de3.lengthInByte + de3.data
        val dataLength = (dataWithoutLengthAndChecksum.length + 2) / 2 // + 2 for checksum
        val dataWithoutChecksum = toHex(dataLength, 2) + dataWithoutLengthAndChecksum

        var xorChecksum = 0
        val xorByteData = dataWithoutChecksum.map { parseIntToHex(it) }
        xorByteData.forEach { xorChecksum = xorChecksum xor it }

        val xorChecksumString = toHex(xorChecksum, 1)

        val parsedDataSet = dataWithoutChecksum + luhnChecksum + xorChecksumString

        return Flickercode(challengeHHD_UC, parsedDataSet)
    }

    protected open fun parseDatenelement(code: String, startIndex: Int): FlickercodeDatenelement {
        val lengthByteLength = 2
        val dataElementAndRest = code.substring(startIndex)

        if (dataElementAndRest.isEmpty() || dataElementAndRest.length < lengthByteLength) { // data element not set
            return FlickercodeDatenelement("", "", FlickercodeEncoding.BCD, startIndex)
        }

        val lengthByteString = dataElementAndRest.substring(0, lengthByteLength)
        val lengthByte = lengthByteString.toInt()

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
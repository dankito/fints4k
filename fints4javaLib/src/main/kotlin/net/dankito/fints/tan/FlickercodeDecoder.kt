package net.dankito.fints.tan

import kotlin.math.floor


open class FlickercodeDecoder {

    open fun decodeChallenge(challenge: String): Flickercode {
        var code = challenge.toUpperCase().replace ("[^a-fA-F0-9]", "")

        val challengeLength = parseIntToHex(challenge.substring(0, 2))

        val startCodeLengthByte = parseIntToHex(challenge.substring(2, 4))
        val hasControlByte = isBitSet(startCodeLengthByte, 7)
        val startCodeEncoding = if (isBitNotSet(startCodeLengthByte, 6)) FlickercodeEncoding.BCD else FlickercodeEncoding.ASCII
        val startCodeLength = startCodeLengthByte and 0b00011111 // TODO: is this correct?

        val controlByte = "" // TODO (there can be multiple of them!)

        val startCodeStartIndex = if (hasControlByte) 6 else 4
        val startCodeEndIndex = startCodeStartIndex + startCodeLength
        val startCode = code.substring(startCodeStartIndex, startCodeEndIndex)

        val de1 = "" // TODO
        val de2 = "" // TODO
        val de3 = "" // TODO

        var luhnData = controlByte + startCode + de1 + de2 + de3
        if (luhnData.length % 2 != 0) {
            luhnData = luhnData + "F" // for Luhn checksum it's required to have full bytes // TODO: should be incorrect. E.g. controlByte has to be checked / stuffed to full byte
        }

        val luhnSum = luhnData.mapIndexed { index, char ->
            val asNumber = char.toString().toInt()

            if (index % 2 == 1) {
                val doubled = asNumber * 2
                return@mapIndexed (doubled / 10) + (doubled % 10)
            }

            asNumber
        }.sum()

        val luhnChecksum = 10 - (luhnSum % 10)

        val countStartCodeBytes = startCodeLength / 2
        val dataWithoutLengthAndChecksum = toHex(countStartCodeBytes, 2) + controlByte + startCode + de1 + de2 + de3 // TODO add length of de1-3 (for controlByte as well?)
        val dataLength = (dataWithoutLengthAndChecksum.length + 2) / 2 // + 2 for checksum
        val dataWithoutChecksum = toHex(dataLength, 2) + dataWithoutLengthAndChecksum
        val xorByteData = dataWithoutChecksum.map { parseIntToHex(it) }

        var xorChecksum = 0
        xorByteData.forEach { xorChecksum = xorChecksum xor it }

        val xorChecksumString = toHex(xorChecksum, 1)

        val rendered = dataWithoutChecksum + luhnChecksum + xorChecksumString


        /* length check: first byte */
        val len = code.length / 2 - 1
        code = toHex(len, 2) + code.substring(2)

        /* luhn checksum */
        val luhndata = getPayload(code)
        var luhnsum = 0
        var i = 0

        while (i < luhndata.length) {
            luhnsum += 1 * parseIntToHex(luhndata[i]) + quersumme(2 * parseIntToHex(luhndata[i + 1]))
            i += 2
        }

        luhnsum = (10 - luhnsum % 10) % 10
        code = code.substring(0, code.length - 2) + toHex(luhnsum, 1) + code.substring(code.length - 1)

        /* xor checksum */
        var xorsum = 0
        i = 0
        while (i < code.length - 2) {
            xorsum = xorsum xor parseIntToHex(code[i])
            i++
        }

        code = code.substring(0, code.length - 1) + toHex(xorsum, 1)

        return Flickercode(challenge, challengeLength, hasControlByte, startCodeEncoding, startCodeLength, startCode, luhnChecksum, toHex(xorChecksum, 1), rendered)
    }

    open fun toHex(number: Int, minLength: Int): String {
        var result = number.toString (16).toUpperCase()

        while (result.length < minLength) {
            result = '0' + result
        }

        return result
    }

    fun quersumme(number: Int): Int {
        var quersumme = 0
        var temp = number

        while (temp != 0) {
            quersumme += temp % 10
            temp = floor(temp / 10.0).toInt()
        }

        return quersumme
    }

    fun getPayload(code: String): String {
        var i = 0
        var payload = ""

        var len = parseIntToHex(code.substring(0, 2))
        i += 2

        while (i < code.length-2) {
            /* skip bcd identifier */
            i += 1
            /* parse length */
            len = parseIntToHex(code.substring(i, i + 1))
            i += 1
            // i = 4
            var endIndex = i + len * 2
            if (endIndex > code.length) {
                endIndex = code.length
            }
            payload += code.substring(i, endIndex)
            i += len * 2
        }

        return payload
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

    protected open fun isBitNotSet(num: Int, bit: Int): Boolean {
        return num and (1 shl bit) == 0
    }

}
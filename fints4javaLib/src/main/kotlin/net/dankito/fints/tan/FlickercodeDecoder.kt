package net.dankito.fints.tan

import kotlin.math.floor


open class FlickercodeDecoder {

    open fun decodeChallenge(challenge: String): String {
        var code = challenge.toUpperCase().replace ("[^a-fA-F0-9]", "")

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

        return code
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

}
package net.dankito.banking.fints.util


open class Base64 {

    companion object {

        const val Base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

        val NonBase64CharsRegex = Regex("[^=$Base64Chars]")

    }


    open fun encode(string: String): String {
        val paddingLength = when (string.length % 3) {
            1 -> 2
            2 -> 1
            else -> 0
        }

        var padding = ""
        var paddedString = string

        (0 until paddingLength).forEach {
            padding += "="
            paddedString += 0.toChar()
        }

        val encoded = encodePaddedString(paddedString)

        return encoded.dropLast(paddingLength) + padding
    }

    protected open fun encodePaddedString(string: String): String {
        val encoded = StringBuilder(string.length)

        (string.indices step 3).forEach { index ->
            val number: Int =
                (0xFF.and(string[index    ].toInt()) shl 16) +
                (0xFF.and(string[index + 1].toInt()) shl  8) +
                 0xFF.and(string[index + 2].toInt())

            encoded.append(Base64Chars[(number shr 18) and 0x3F])
            encoded.append(Base64Chars[(number shr 12) and 0x3F])
            encoded.append(Base64Chars[(number shr  6) and 0x3F])
            encoded.append(Base64Chars[ number         and 0x3F])
        }

        return encoded.toString()
    }


    open fun decode(string: String): String {
        if (string.length % 4 != 0) {
            throw IllegalArgumentException("The string \"$string\" has an illegal length, has to be a multiple of four.")
        }

        val paddingLength = when {
            string.length >= 2 && string[string.length - 2] == '=' -> 2
            string.length >= 1 && string[string.length - 1] == '=' -> 1
            else -> 0
        }

        // replace non-Base64 characters like \r, \n, ... and padding character with A (= 0)
        val clean = string.replace(NonBase64CharsRegex, "").replace("=", "A")

        val decoded = decodeCleanedString(clean)

        return decoded.dropLast(paddingLength)
    }

    protected open fun decodeCleanedString(string: String): String {
        val decoded = StringBuilder()

        (string.indices step 4).forEach { index ->
            val number: Int =
                (Base64Chars.indexOf(string[index    ]) shl 18) +
                (Base64Chars.indexOf(string[index + 1]) shl 12) +
                (Base64Chars.indexOf(string[index + 2]) shl  6) +
                 Base64Chars.indexOf(string[index + 3])

            decoded.append(0xFF.and(number shr 16).toChar())
            decoded.append(0xFF.and(number shr 8).toChar())
            decoded.append(0xFF.and(number).toChar())
        }

        return decoded.toString()
    }

}
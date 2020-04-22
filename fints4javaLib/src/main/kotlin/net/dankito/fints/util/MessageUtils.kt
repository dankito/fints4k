package net.dankito.fints.util

import net.dankito.fints.messages.Separators
import net.dankito.fints.response.ResponseParser
import net.dankito.utils.extensions.allIndicesOf
import java.util.regex.Matcher
import java.util.regex.Pattern


open class MessageUtils {

    companion object {
        val BinaryDataHeaderPattern = Pattern.compile("@\\d+@")
    }


    open fun findSeparatorIndices(dataString: String, separator: String): List<Int> {
        return findSeparatorIndices(dataString, separator, findBinaryDataRanges(dataString))
    }

    open fun findSeparatorIndices(dataString: String, separator: String, binaryDataRanges: List<IntRange>): List<Int> {
        return dataString.allIndicesOf(separator)
            .filter { isCharacterMasked(it, dataString) == false }
            .filter { isInRange(it, binaryDataRanges) == false }
    }

    open fun findBinaryDataRanges(dataString: String): List<IntRange> {
        val binaryDataRanges = mutableListOf<IntRange>()
        val binaryDataMatcher = BinaryDataHeaderPattern.matcher(dataString)

        while (binaryDataMatcher.find()) {
            if (isEncryptionDataSegment(dataString, binaryDataMatcher) == false) {
                val startIndex = binaryDataMatcher.end()
                val length = binaryDataMatcher.group().replace("@", "").toInt()

                binaryDataRanges.add(IntRange(startIndex, startIndex + length - 1))
            }
        }
        return binaryDataRanges
    }

    open fun isEncryptionDataSegment(dataString: String, binaryDataMatcher: Matcher): Boolean {
        val binaryDataHeaderStartIndex = binaryDataMatcher.start()

        if (binaryDataHeaderStartIndex > 15) {
            val encryptionDataSegmentMatcher = ResponseParser.EncryptionDataSegmentHeaderPattern.matcher(dataString)

            if (encryptionDataSegmentMatcher.find(binaryDataHeaderStartIndex - 15)) {
                return encryptionDataSegmentMatcher.start() < binaryDataHeaderStartIndex
            }
        }

        return false
    }

    open fun isCharacterMasked(characterIndex: Int, wholeString: String): Boolean {
        if (characterIndex > 0) {
            val previousChar = wholeString[characterIndex - 1]

            return previousChar.toString() == Separators.MaskingCharacter
        }

        return false
    }

    open fun doesNotMaskSeparatorOrMaskingCharacter(maskingCharacterIndex: Int, messagePart: String): Boolean {
        if (maskingCharacterIndex < messagePart.length - 1) {
            val nextCharacter = messagePart[maskingCharacterIndex + 1]

            return Separators.AllSeparatorsAndMaskingCharacter.contains(nextCharacter.toString()) == false
        }

        return true
    }

    open fun isInRange(index: Int, ranges: List<IntRange>): Boolean {
        for (range in ranges) {
            if (range.contains(index)) {
                return true
            }
        }

        return false
    }

    open fun maskCharacterAtIndices(unmaskedString: String, unmaskedCharacter: String, indices: List<Int>): String {
        var maskedString = unmaskedString

        indices.sortedDescending().forEach { index ->
            maskedString = maskedString.replaceRange(index, index + 1, Separators.MaskingCharacter + unmaskedCharacter
            )
        }

        return maskedString
    }

}
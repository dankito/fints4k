package net.dankito.banking.fints.util

import net.dankito.banking.fints.messages.Separators
import net.dankito.banking.fints.response.ResponseParser


open class MessageUtils {

    companion object {
        val BinaryDataHeaderPattern = Regex("@\\d+@")

        val EncryptionDataSegmentHeaderRegex = Regex("${MessageSegmentId.EncryptionData.id}:\\d{1,3}:\\d{1,3}\\+")
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

        return BinaryDataHeaderPattern.findAll(dataString).mapNotNull { matchResult ->
            if (isEncryptionDataSegment(dataString, matchResult) == false) {
                val startIndex = matchResult.range.last + 1
                val length = matchResult.value.replace(Separators.BinaryDataSeparator, "").toInt()

                return@mapNotNull IntRange(startIndex, startIndex + length - 1)
            }

            null
        }.toList()
    }

    open fun isEncryptionDataSegment(dataString: String, binaryDataMatcher: MatchResult): Boolean {
        val binaryDataHeaderStartIndex = binaryDataMatcher.range.start

        if (binaryDataHeaderStartIndex > 15) {

            EncryptionDataSegmentHeaderRegex.find(dataString, binaryDataHeaderStartIndex - 15)?.let { matchResult ->
                return matchResult.range.start < binaryDataHeaderStartIndex
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


    // TODO: move to a library
    protected open fun String.allIndicesOf(toFind: String): List<Int> {
        val indices = mutableListOf<Int>()
        var index = -1

        do {
            index = this.indexOf(toFind, index + 1)

            if (index > -1) {
                indices.add(index)
            }
        } while (index > -1)

        return indices
    }

}
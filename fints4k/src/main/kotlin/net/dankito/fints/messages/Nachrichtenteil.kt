package net.dankito.fints.messages

import net.dankito.fints.util.MessageUtils


abstract class Nachrichtenteil(protected val messageUtils: MessageUtils = MessageUtils()) {

    abstract fun format(): String


    open fun maskMessagePart(messagePart: String, separators: List<String>): String {

        var maskedMessagePart = messagePart
        val binaryDataRanges = messageUtils.findBinaryDataRanges(messagePart)

        val unmaskedMaskingCharacterIndices = messageUtils.findSeparatorIndices(maskedMessagePart, Separators.MaskingCharacter, binaryDataRanges)
            .filter { messageUtils.doesNotMaskSeparatorOrMaskingCharacter(it, maskedMessagePart) }

        maskedMessagePart = messageUtils
            .maskCharacterAtIndices(maskedMessagePart, Separators.MaskingCharacter, unmaskedMaskingCharacterIndices)


        separators.forEach { separator ->
            maskedMessagePart = maskSeparators(maskedMessagePart, separator, binaryDataRanges)
        }

        maskedMessagePart = maskSeparators(maskedMessagePart, Separators.DataElementGroupsSeparator, binaryDataRanges)

        return maskedMessagePart
    }

    protected open fun maskSeparators(unmaskedMessagePart: String, separator: String, binaryDataRanges: List<IntRange>): String {

        val separatorIndices = messageUtils.findSeparatorIndices(unmaskedMessagePart, separator, binaryDataRanges)

        if (separatorIndices.isNotEmpty()) {
            return messageUtils.maskCharacterAtIndices(unmaskedMessagePart, separator, separatorIndices)
        }

        return unmaskedMessagePart
    }

}
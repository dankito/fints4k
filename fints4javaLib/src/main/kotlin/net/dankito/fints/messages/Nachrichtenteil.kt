package net.dankito.fints.messages

import net.dankito.fints.util.MessageUtils


abstract class Nachrichtenteil(protected val messageUtils: MessageUtils = MessageUtils()) {

    abstract fun format(): String


    open fun maskMessagePart(messagePart: String, separator: String): String {

        var maskedMessagePart = messagePart
        val binaryDataRanges = messageUtils.findBinaryDataRanges(messagePart)

        val unmaskedMaskingCharacterIndices = messageUtils.findSeparatorIndices(maskedMessagePart, Separators.MaskingCharacter, binaryDataRanges)
            .filter { messageUtils.doesNotMaskSeparatorOrMaskingCharacter(it, maskedMessagePart) }

        maskedMessagePart = messageUtils
            .maskCharacterAtIndices(maskedMessagePart, Separators.MaskingCharacter, unmaskedMaskingCharacterIndices)


        val separatorIndices = messageUtils.findSeparatorIndices(maskedMessagePart, separator, binaryDataRanges)

        return messageUtils.maskCharacterAtIndices(maskedMessagePart, separator, separatorIndices)
    }

}
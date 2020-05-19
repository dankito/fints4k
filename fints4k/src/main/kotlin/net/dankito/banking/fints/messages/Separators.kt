package net.dankito.banking.fints.messages


class Separators {

    companion object {
        const val SegmentSeparatorChar = '\''
        const val SegmentSeparator = SegmentSeparatorChar.toString()

        const val DataElementGroupsSeparatorChar = '+'
        const val DataElementGroupsSeparator = DataElementGroupsSeparatorChar.toString()

        const val DataElementsSeparatorChar = ':'
        const val DataElementsSeparator = DataElementsSeparatorChar.toString()

        const val BinaryDataSeparatorChar = '@'
        const val BinaryDataSeparator = BinaryDataSeparatorChar.toString()

        const val MaskingCharacterChar = '?'
        const val MaskingCharacter = MaskingCharacterChar.toString()

        val AllSeparators = listOf(DataElementsSeparator, DataElementGroupsSeparator, SegmentSeparator)

        val AllSeparatorsAndMaskingCharacter = listOf(*AllSeparators.toTypedArray(), MaskingCharacter)
    }

}
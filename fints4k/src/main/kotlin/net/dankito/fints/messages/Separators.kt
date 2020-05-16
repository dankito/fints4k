package net.dankito.fints.messages


class Separators {

    companion object {
        const val SegmentSeparator = "'"

        const val DataElementGroupsSeparator = "+"

        const val DataElementsSeparator = ":"

        const val BinaryDataSeparatorChar = '@'
        const val BinaryDataSeparator = BinaryDataSeparatorChar.toString()

        const val MaskingCharacter = "?"

        val AllSeparators = listOf(DataElementsSeparator, DataElementGroupsSeparator, SegmentSeparator)

        val AllSeparatorsAndMaskingCharacter = listOf(*AllSeparators.toTypedArray(), MaskingCharacter)
    }

}
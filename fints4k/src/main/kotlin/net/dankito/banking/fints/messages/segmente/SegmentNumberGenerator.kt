package net.dankito.banking.fints.messages.segmente

import net.dankito.banking.fints.messages.segmente.ISegmentNumberGenerator.Companion.FirstSegmentNumber


open class SegmentNumberGenerator : ISegmentNumberGenerator {

    protected var currentSegmentNumber = 0


    override fun resetSegmentNumber(countNumbersToSkipForHeaders: Int): Int {
        currentSegmentNumber = FirstSegmentNumber + countNumbersToSkipForHeaders

        return currentSegmentNumber
    }

    override fun getNextSegmentNumber(): Int {
        return ++currentSegmentNumber
    }


    override fun toString(): String {
        return "Current segment number = $currentSegmentNumber"
    }

}
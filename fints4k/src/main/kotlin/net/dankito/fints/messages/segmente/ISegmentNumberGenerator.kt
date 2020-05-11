package net.dankito.fints.messages.segmente


interface ISegmentNumberGenerator {

    companion object {
        const val FirstSegmentNumber = 1
    }


    fun resetSegmentNumber(countNumberToSkipForHeader: Int): Int

    fun getNextSegmentNumber(): Int

}
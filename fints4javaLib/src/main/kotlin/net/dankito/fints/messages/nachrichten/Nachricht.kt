package net.dankito.fints.messages.nachrichten

import net.dankito.fints.messages.Nachrichtenteil
import net.dankito.fints.messages.segmente.Segment


open class Nachricht(val segments: List<Segment>) : Nachrichtenteil() {

    companion object {
        const val SegmentSeparator = "'"
    }


    override fun format(): String {
        return segments.joinToString(SegmentSeparator, postfix = SegmentSeparator) { it.format() }
    }


}
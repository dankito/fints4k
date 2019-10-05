package net.dankito.fints.messages.segmente

import net.dankito.fints.messages.segmente.id.ISegmentId


enum class SegmentId(override val id: String) : ISegmentId {

    DialogEnd("HKEND"),

    ProcessingPreparation("HKVVB"),

    Identification("HKIDN"),

    Tan("HKTAN")

}
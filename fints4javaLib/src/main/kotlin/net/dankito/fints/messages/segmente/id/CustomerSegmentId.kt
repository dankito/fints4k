package net.dankito.fints.messages.segmente.id


enum class CustomerSegmentId(override val id: String) : ISegmentId {

    DialogEnd("HKEND"),

    ProcessingPreparation("HKVVB"),

    Identification("HKIDN"),

    Tan("HKTAN")

}
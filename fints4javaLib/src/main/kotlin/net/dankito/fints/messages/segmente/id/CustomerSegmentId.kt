package net.dankito.fints.messages.segmente.id


enum class CustomerSegmentId(override val id: String) : ISegmentId {

    DialogEnd("HKEND"),

    ProcessingPreparation("HKVVB"),

    Identification("HKIDN"),

    Synchronization("HKSYN"),

    Tan("HKTAN"),

    Balance("HKSAL"),

    AccountTransactionsMt940("HKKAZ")

}
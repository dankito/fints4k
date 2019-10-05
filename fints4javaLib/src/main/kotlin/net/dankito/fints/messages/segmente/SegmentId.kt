package net.dankito.fints.messages.segmente


enum class SegmentId(val id: String) {

    MessageHeader("HNHBK"),

    MessageEnding("HNHBS"),

    EncryptionHeader("HNVSK"),

    EncryptionData("HNVSD"),

    SignatureHeader("HNSHK"),

    SignatureEnding("HNSHA"),

    DialogEnd("HKEND"),

    ProcessingPreparation("HKVVB"),

    Identification("HKIDN"),

    Tan("HKTAN")

}
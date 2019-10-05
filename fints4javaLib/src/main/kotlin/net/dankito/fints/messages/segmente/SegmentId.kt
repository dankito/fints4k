package net.dankito.fints.messages.segmente


enum class SegmentId(val id: String) {

    MessageHeader("HNHBK"),

    MessageClosing("HNHBS"),

    EncryptionHeader("HNVSK"),

    EncryptionData("HNVSD"),

    SignatureHeader("HNSHK"),

    SignatureClosing("HNSHA"),

    DialogEnd("HKEND"),

    ProcessingPreparation("HKVVB"),

    Identification("HKIDN"),

    Tan("HKTAN")

}
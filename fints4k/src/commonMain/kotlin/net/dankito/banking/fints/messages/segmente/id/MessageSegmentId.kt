package net.dankito.banking.fints.messages.segmente.id


enum class MessageSegmentId(override val id: String) : ISegmentId {

    MessageHeader("HNHBK"),

    MessageEnding("HNHBS"),

    EncryptionHeader("HNVSK"),

    EncryptionData("HNVSD"),

    SignatureHeader("HNSHK"),

    SignatureEnding("HNSHA")

}
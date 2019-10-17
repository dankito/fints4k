package net.dankito.fints.response

import net.dankito.fints.messages.segmente.id.ISegmentId


enum class InstituteSegmentId(override val id: String) : ISegmentId {

    MessageFeedback("HIRMG"),

    SegmentFeedback("HIRMS"),

    Synchronization("HISYN"),

    BankParameters("HIBPA"),

    SecurityMethods("HISHV"),

    CommunicationInfo("HIKOM"),

    UserParameters("HIUPA"),

    AccountInfo("HIUPD"),

    SepaAccountInfo("HISPA"),

    TanInfo("HITANS"),

    Tan("HITAN"),

    Balance("HISAL"),

    AccountTransactionsMt940("HIKAZ")

}
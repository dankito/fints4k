package net.dankito.fints.response

import net.dankito.fints.messages.segmente.id.ISegmentId


enum class InstituteSegmentId(override val id: String) : ISegmentId {

    Synchronization("HISYN"),

    BankParameters("HIBPA"),

    SecurityMethods("HISHV"),

    UserParameters("HIUPA"),

    AccountInfo("HIUPD"),

    TanInfo("HITANS"),

    Balance("HISAL"),

    AccountTransactionsMt940("HIKAZ")

}
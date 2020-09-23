package net.dankito.banking.fints.response

import net.dankito.banking.fints.messages.segmente.id.ISegmentId


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

    SepaAccountInfoParameters("HISPAS"),

    PinInfo("HIPINS"),

    TanInfo("HITANS"),

    Tan("HITAN"),

    TanMediaList("HITAB"),

    ChangeTanMediaParameters("HITAUS"), // there's no response data segment for HKTAU -> HITAU does not exist

    Balance("HISAL"),

    AccountTransactionsMt940("HIKAZ"),

    AccountTransactionsMt940Parameters(AccountTransactionsMt940.id + "S"),

    CreditCardTransactions("DIKKU")

}
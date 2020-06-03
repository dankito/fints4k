package net.dankito.banking.fints.messages.segmente.id


enum class CustomerSegmentId(override val id: String) : ISegmentId {

    DialogEnd("HKEND"),

    ProcessingPreparation("HKVVB"),

    Identification("HKIDN"),

    Synchronization("HKSYN"),

    Tan("HKTAN"),

    TanMediaList("HKTAB"),

    ChangeTanMedium("HKTAU"),

    Balance("HKSAL"),

    AccountTransactionsMt940("HKKAZ"),

    SepaBankTransfer("HKCCS"),

    SepaInstantPaymentBankTransfer("HKIPZ"),

    SepaAccountInfoParameters("HKSPA") // not implemented, retrieved automatically with UPD

}
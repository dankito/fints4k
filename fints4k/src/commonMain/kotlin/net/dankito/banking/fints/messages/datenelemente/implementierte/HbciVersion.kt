package net.dankito.banking.fints.messages.datenelemente.implementierte


enum class HbciVersion(val versionNumber: Int, override val code: String) : ICodeEnum {

    Hbci_2_0_1(201, "201"),

    Hbci_2_1_0(210, "210"),

    Hbci_2_2_0(220, "220"),

    FinTs_3_0_0(300, "300"),

    FinTs_4_0_0(400, "400")

}
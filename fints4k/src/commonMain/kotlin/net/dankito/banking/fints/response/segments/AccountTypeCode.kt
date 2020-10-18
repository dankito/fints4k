package net.dankito.banking.fints.response.segments

import net.dankito.banking.fints.messages.datenelemente.implementierte.ICodeEnum


/**
 * Klassifizierung der Konten. Innerhalb der vorgegebenen Codebereiche sind kreditinstitutsindividuell bei Bedarf 
 * weitere Kontoarten möglich.
 * 
 * Codierung:
 * - 1 – 9: Kontokorrent-/Girokonto
 * - 10 – 19: Sparkonto
 * - 20 –29: Festgeldkonto (Termineinlagen)
 * - 30 – 39: Wertpapierdepot
 * - 40 –49: Kredit-/Darlehenskonto
 * - 50 – 59: Kreditkartenkonto
 * - 60 – 69: Fonds-Depot bei einer Kapitalanlagegesellschaft
 * - 70 – 79: Bausparvertrag
 * - 80 – 89: Versicherungsvertrag
 * - 90 – 99: Sonstige (nicht zuordenbar)
 */
enum class AccountTypeCode(override val code: String, val type: AccountType) : ICodeEnum {

    Girokonto_0("0", AccountType.Girokonto), // not stated in spezification, but Sparkasse sometimes gives a Girokonto the type code '0'

    Girokonto_1("1", AccountType.Girokonto),

    Girokonto_2("2", AccountType.Girokonto),

    Girokonto_3("3", AccountType.Girokonto),

    Girokonto_4("4", AccountType.Girokonto),

    Girokonto_5("5", AccountType.Girokonto),

    Girokonto_6("6", AccountType.Girokonto),

    Girokonto_7("7", AccountType.Girokonto),

    Girokonto_8("8", AccountType.Girokonto),

    Girokonto_9("9", AccountType.Girokonto),

    Sparkonto_1("10", AccountType.Sparkonto),

    Sparkonto_2("11", AccountType.Sparkonto),

    Sparkonto_3("12", AccountType.Sparkonto),

    Sparkonto_4("13", AccountType.Sparkonto),

    Sparkonto_5("14", AccountType.Sparkonto),

    Sparkonto_6("15", AccountType.Sparkonto),

    Sparkonto_7("16", AccountType.Sparkonto),

    Sparkonto_8("17", AccountType.Sparkonto),

    Sparkonto_9("18", AccountType.Sparkonto),

    Sparkonto_10("19", AccountType.Sparkonto),

    Festgeldkonto_1("20", AccountType.Festgeldkonto),

    Festgeldkonto_2("21", AccountType.Festgeldkonto),

    Festgeldkonto_3("22", AccountType.Festgeldkonto),

    Festgeldkonto_4("23", AccountType.Festgeldkonto),

    Festgeldkonto_5("24", AccountType.Festgeldkonto),

    Festgeldkonto_6("25", AccountType.Festgeldkonto),

    Festgeldkonto_7("26", AccountType.Festgeldkonto),

    Festgeldkonto_8("27", AccountType.Festgeldkonto),

    Festgeldkonto_9("28", AccountType.Festgeldkonto),

    Festgeldkonto_10("29", AccountType.Festgeldkonto),

    Wertpapierdepot_1("30", AccountType.Wertpapierdepot),

    Wertpapierdepot_2("31", AccountType.Wertpapierdepot),

    Wertpapierdepot_3("32", AccountType.Wertpapierdepot),

    Wertpapierdepot_4("33", AccountType.Wertpapierdepot),

    Wertpapierdepot_5("34", AccountType.Wertpapierdepot),

    Wertpapierdepot_6("35", AccountType.Wertpapierdepot),

    Wertpapierdepot_7("36", AccountType.Wertpapierdepot),

    Wertpapierdepot_8("37", AccountType.Wertpapierdepot),

    Wertpapierdepot_9("38", AccountType.Wertpapierdepot),

    Wertpapierdepot_10("39", AccountType.Wertpapierdepot),

    Darlehenskonto_1("40", AccountType.Darlehenskonto),

    Darlehenskonto_2("41", AccountType.Darlehenskonto),

    Darlehenskonto_3("42", AccountType.Darlehenskonto),

    Darlehenskonto_4("43", AccountType.Darlehenskonto),

    Darlehenskonto_5("44", AccountType.Darlehenskonto),

    Darlehenskonto_6("45", AccountType.Darlehenskonto),

    Darlehenskonto_7("46", AccountType.Darlehenskonto),

    Darlehenskonto_8("47", AccountType.Darlehenskonto),

    Darlehenskonto_9("48", AccountType.Darlehenskonto),

    Darlehenskonto_10("49", AccountType.Darlehenskonto),

    Kreditkartenkonto_1("50", AccountType.Kreditkartenkonto),

    Kreditkartenkonto_2("51", AccountType.Kreditkartenkonto),

    Kreditkartenkonto_3("52", AccountType.Kreditkartenkonto),

    Kreditkartenkonto_4("53", AccountType.Kreditkartenkonto),

    Kreditkartenkonto_5("54", AccountType.Kreditkartenkonto),

    Kreditkartenkonto_6("55", AccountType.Kreditkartenkonto),

    Kreditkartenkonto_7("56", AccountType.Kreditkartenkonto),

    Kreditkartenkonto_8("57", AccountType.Kreditkartenkonto),

    Kreditkartenkonto_9("58", AccountType.Kreditkartenkonto),

    Kreditkartenkonto_10("59", AccountType.Kreditkartenkonto),

    FondsDepot_1("60", AccountType.FondsDepot),

    FondsDepot_2("61", AccountType.FondsDepot),

    FondsDepot_3("62", AccountType.FondsDepot),

    FondsDepot_4("63", AccountType.FondsDepot),

    FondsDepot_5("64", AccountType.FondsDepot),

    FondsDepot_6("65", AccountType.FondsDepot),

    FondsDepot_7("66", AccountType.FondsDepot),

    FondsDepot_8("67", AccountType.FondsDepot),

    FondsDepot_9("68", AccountType.FondsDepot),

    FondsDepot_10("69", AccountType.FondsDepot),

    Bausparvertrag_1("70", AccountType.Bausparvertrag),

    Bausparvertrag_2("71", AccountType.Bausparvertrag),

    Bausparvertrag_3("72", AccountType.Bausparvertrag),

    Bausparvertrag_4("73", AccountType.Bausparvertrag),

    Bausparvertrag_5("74", AccountType.Bausparvertrag),

    Bausparvertrag_6("75", AccountType.Bausparvertrag),

    Bausparvertrag_7("76", AccountType.Bausparvertrag),

    Bausparvertrag_8("77", AccountType.Bausparvertrag),

    Bausparvertrag_9("78", AccountType.Bausparvertrag),

    Bausparvertrag_10("79", AccountType.Bausparvertrag),

    Versicherungsvertrag_1("80", AccountType.Versicherungsvertrag),

    Versicherungsvertrag_2("81", AccountType.Versicherungsvertrag),

    Versicherungsvertrag_3("82", AccountType.Versicherungsvertrag),

    Versicherungsvertrag_4("83", AccountType.Versicherungsvertrag),

    Versicherungsvertrag_5("84", AccountType.Versicherungsvertrag),

    Versicherungsvertrag_6("85", AccountType.Versicherungsvertrag),

    Versicherungsvertrag_7("86", AccountType.Versicherungsvertrag),

    Versicherungsvertrag_8("87", AccountType.Versicherungsvertrag),

    Versicherungsvertrag_9("88", AccountType.Versicherungsvertrag),

    Versicherungsvertrag_10("89", AccountType.Versicherungsvertrag),

    Sonstige_1("90", AccountType.Sonstige),

    Sonstige_2("91", AccountType.Sonstige),

    Sonstige_3("92", AccountType.Sonstige),

    Sonstige_4("93", AccountType.Sonstige),

    Sonstige_5("94", AccountType.Sonstige),

    Sonstige_6("95", AccountType.Sonstige),

    Sonstige_7("96", AccountType.Sonstige),

    Sonstige_8("97", AccountType.Sonstige),

    Sonstige_9("98", AccountType.Sonstige),

    Sonstige_10("99", AccountType.Sonstige)
    
}
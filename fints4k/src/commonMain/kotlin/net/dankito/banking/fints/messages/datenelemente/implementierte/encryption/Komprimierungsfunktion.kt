package net.dankito.banking.fints.messages.datenelemente.implementierte.encryption

import net.dankito.banking.fints.messages.datenelemente.implementierte.ICodeEnum


enum class Komprimierungsfunktion(val abbreviation: String, override val code: String) : ICodeEnum {

    Keine_Kompression("NULL", "0"),
    
    Lempel_Ziv_Welch("LZW", "1"),

    Optimized_LZW("COM", "2"),

    Lempel_Ziv("LZSS", "3"),

    LZ_Huffman_Coding("LZHuf", "4"),

    PKZIP("ZIP", "5"),

    deflate("GZIP", "6"),

    bzip2("bzip2", "7"),

    Gegenseitig_vereinbart("ZZZ", "999")

}
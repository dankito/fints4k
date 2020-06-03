package net.dankito.banking.fints.messages.datenelemente.implementierte.encryption

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.banking.fints.messages.datenelemente.implementierte.allCodes


/**
 * Code der unterstützten Komprimierungsfunktion.
 *
 * Codierung:
 * 0: Keine Kompression (NULL)
 * 1: Lempel, Ziv, Welch (LZW)
 * 2: Optimized LZW (COM)
 * 3: Lempel, Ziv (LZSS)
 * 4: LZ + Huffman Coding (LZHuf)
 * 5: PKZIP (ZIP)
 * 6: deflate (GZIP) (http://www.gzip.org/zlib)
 * 7: bzip2 (http://sourceware.cygnus.com/bzip2/)
 * 999: Gegenseitig vereinbart (ZZZ)
 */
open class KomprimierungsfunktionDatenelement(algorithm: Komprimierungsfunktion)
    : Code(algorithm.code, AllowedValues, Existenzstatus.Mandatory) {

    companion object {
        val AllowedValues = allCodes<Komprimierungsfunktion>()
    }

}
package net.dankito.banking.fints.messages.datenelemente.implementierte.tan

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.BinaerDatenelement


/**
 * Er enthält im Falle des Zwei-Schritt-TAN-Verfahrens bei TAN-Prozess=1 den Hashwert über die
 * Daten eines Kundenauftrags (z. B. „HKCCS“). Dieser wird z. B. im Rahmen des Geschäftsvorfalls
 * HKTAN vom Kunden übermittelt und vom Kreditinstitut in der Antwortnachricht HITAN gespiegelt.
 *
 * Das vom Institut verwendete Auftrags-Hashwertverfahren wird in der BPD übermittelt. In der
 * vorliegenden Version wird RIPEMD-160 verwendet.
 *
 * In die Berechnung des Auftrags-Hashwerts geht der Bereich vom ersten bit des Segmentkopfes
 * bis zum letzten bit des Trennzeichens ein.
 *
 * RIPEMD-160
 *
 * Der Hash-Algorithmus RIPEMD-160 bildet Eingabe-Bitfolgen beliebiger Länge auf einen als
 * Bytefolge dargestellten Hash-Wert von 20 Byte (160 Bit) Länge ab. Teil des Hash-Algorithmus
 * ist das Padding von Eingabe-Bitfolgen auf ein Vielfaches von 64 Byte. Das Padding erfolgt
 * auch dann, wenn die Eingabe-Bitfolge bereits eine Länge hat, die ein Vielfaches von 64 Byte ist.
 * RIPEMD-160 verarbeitet die Eingabe-Bitfolgen in Blöcken von 64 Byte Länge.
 *
 * Als Initialisierungsvektor dient die binäre Zeichenfolge
 * X’01 23 45 67 89 AB CD EF FE DC BA 98 76 54 32 10 F0 E1 D2 C3’.
 */
open class AuftragsHashwert(hash: String, existenzstatus: Existenzstatus)
    : BinaerDatenelement(hash, existenzstatus, 256)
package net.dankito.fints.messages.datenelemente.implementierte.tan

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Dieses Datenelement enthält im Falle des Zwei-Schritt-TAN-Verfahrens die Challenge zu einem
 * eingereichten Auftrag. Aus der Challenge wird vom Kunden die eigentliche TAN ermittelt.
 * Die Challenge wird unabhängig vom Prozessvariante 1 oder 2 in der Kreditinstitutsantwort im
 * Segment HITAN übermittelt.
 *
 * Ist der BPD-Parameter „Challenge strukturiert“ mit „J“ belegt, so können im Text folgende
 * Formatsteuerzeichen enthalten sein, die kundenseitig entsprechend zu interpretieren sind.
 * Eine Kaskadierung von Steuerzeichen ist nicht erlaubt.
 *
 * <br>             Zeilenumbruch
 * <p>              Neuer Absatz
 * <b> ... </b>     Fettdruck
 * <i> ... </i>     Kursivdruck
 * <u> ... </u>     Unterstreichen
 * <ul> ... </ul>   Beginn / Ende Aufzählung
 * <ol> ... </ol>   Beginn / Ende Nummerierte Liste
 * <li> ... </li>   Listenelement einer Aufzählung / Nummerierten Liste
 */
open class ChallengeVersion3(challenge: String, existenzstatus: Existenzstatus)
    : AlphanumerischesDatenelement(challenge, existenzstatus, 2048)
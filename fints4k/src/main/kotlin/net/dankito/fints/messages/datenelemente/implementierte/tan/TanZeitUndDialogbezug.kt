package net.dankito.fints.messages.datenelemente.implementierte.tan

import net.dankito.fints.messages.datenelemente.implementierte.ICodeEnum


/**
 * Beschreibung der protokolltechnischen Möglichkeiten, die dem Kunden im Zusammenhang mit Mehrfach-TANs zur
 * Verfügung stehen. Es wird festgelegt, ob die Eingabe der einzelnen TANs zu einem Auftrag durch die unterschiedlichen
 * Benutzer synchron in einem Dialog erfolgen muss oder zeitversetzt in mehreren Dialogen erfolgen kann.
 * Es wird auch festgelegt, ob ein Institut nur eines dieser Verfahren oder beide parallel anbietet. Voraussetzung ist,
 * dass grundsätzlich die Verwendung von Mehrfach-TANs beim Zwei-Schritt-Verfahren erlaubt ist (vgl. Parameter
 * „Mehrfach-TAN erlaubt“). Bei Prozessvariante 1 ist der Parameter immer mit „nicht zutreffend“ zu belegen, da hier
 * generell keine zeitversetzte Verarbeitung möglich ist. Dieser Parameter erweitert den Parameter
 * „TAN zeitversetzt / dialogübergreifend erlaubt“.
 */
enum class TanZeitUndDialogbezug(override val code: String) : ICodeEnum {

    TanNichtZeitversetztDialoguebergreifendErlaubt("1"),

    TanZeitversetztDialoguebergreifendErlaubt("2"),

    BothAllowed("3"),

    NotSupported("4")

}
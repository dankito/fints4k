package net.dankito.banking.fints.messages.datenelemente.implementierte.tan

import net.dankito.banking.fints.messages.datenelemente.implementierte.ICodeEnum


/**
 * Es werden die Möglichkeiten festgelegt, die ein Kunde hat, wenn er für
 * PIN/TAN parallel mehrere TAN-Medien zur Verfügung hat.
 */
enum class TanEinsatzOption(override val code: String) : ICodeEnum {

    KundeKannAlleAktivenMedienParallelNutzen("0"),

    KundeKannGenauEinMediumZuEinerZeitNutzen("1"),

    KundeKannEinMobiltelefonUndEinenTanGeneratorParallelNutzen("2")

}
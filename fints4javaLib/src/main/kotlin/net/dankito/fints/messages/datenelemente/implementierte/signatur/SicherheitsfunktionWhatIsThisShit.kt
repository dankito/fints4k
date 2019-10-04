package net.dankito.fints.messages.datenelemente.implementierte.signatur


/**
 * Ab FinTS 3.0 existieren beim RAH-Verfahren drei Schlüssel (DS-Schlüssel für Non-Repudiation,
 * Signierschlüssel für Authentication und Chiffrierschlüssel für Verschlüsselung) und somit
 * auch drei Sicherheitsfunktionen (Sicherheitsfunktion 1 bei Verwendung des DS-Schlüssels,
 * Sicherheitsfunktion 2 bei Verwendung des Signierschlüssels und Sicherheitsfunktion 4 bei
 * Verwendung des Chiffrierschlüssels) beim RAH-Verfahren.
 *
 * Die Sicherheitsfunktion hat ab FinTS 3.0 lediglich informatorischen Wert, da die eigentliche
 * Steuerung über die Sicherheitsprofile und –Klassen erfolgt.
 *
 * Kodierte Information über die Sicherheitsfunktion, die auf die Nachricht angewendet wird.
 *
 * Codierung:
 * - 1: Non-Repudiation of Origin (NRO)
 * - 2: Message Origin Authentication (AUT)
 * - 4: Encryption, Verschlüsselung und evtl. Komprimierung (ENC)
 */
enum class SicherheitsfunktionWhatIsThisShit(val code: String) {

    NonRepudiationOfOrigin("1"),

    MessageOriginAuthentication("2"),

    Encryption("4")

}
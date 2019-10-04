package net.dankito.fints.messages


/**
 * Der HBCI-Basiszeichensatz baut auf dem international normierten Zeichensatz ISO 8859 auf.
 * Im DE „Unterstützte Sprachen“ in die Bankparameterdaten (s. Kap. D.2) stellt das Kreditinstitut
 * das jeweiligen Codeset des ISO 8859 ein. Ferner wird in die BPD das sprachen-spezifische Subset
 * des ISO 8859 eingestellt. Codeset und Subset definieren gemeinsam den FinTS-Basiszeichensatz.
 * Dieser gilt grundsätzlich für sämtliche nicht-binären Datenelemente. Sofern hiervon aufgrund
 * von Verarbeitungsrestriktionen abgewichen wird, ist dies bei der jeweiligen Formatbeschreibung
 * vermerkt. Für transparente Daten gilt der jeweilige Zeichensatz des Fremdformats.
 *
 * Kreditinstitutsseitig ist jeweils der vollständige erlaubte Zeichensatz zu unterstützen.
 * FinTS-Syntaxzeichen (s. Kap. H.1.1) bleiben von den Zeichensatzvorgaben unberührt (d. h.
 * sind stets erforderlich und mit fester Codierung vorgegeben).
 *
 * Wird ein Auftrag an ein Kreditinstitut übermittelt, der hinsichtlich Zeichensatz und Codierung
 * nicht den Richtlinien entspricht, so ist dieser abzuweisen. Eine kreditinstitutsseitige Korrektur
 * der Auftragsdaten erfolgt nicht.
 */
open class HbciCharset {

    companion object {
        val DefaultCharset = Charsets.ISO_8859_1
    }

}
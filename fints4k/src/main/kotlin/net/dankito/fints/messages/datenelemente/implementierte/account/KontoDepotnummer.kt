package net.dankito.fints.messages.datenelemente.implementierte.account

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Identifikation


/**
 * Identifikationsnummer des Kontos (Kontonummer, Depotnummer, Kreditkartennummer etc.).
 * Das DE dient auch zur Aufnahme von internationalen (alphanumerischen) Kontonummern und
 * zukünftig 20-stelligen Kreditkartenkontonummern.
 *
 * Es ist zu beachten, dass Kontonummern auch führende Nullen beinhalten können, die
 * bankfachlich relevant sind und nicht abgeschnitten werden dürfen.
 */
open class KontoDepotnummer(number: String?, existenzstatus: Existenzstatus) : Identifikation(number, existenzstatus)
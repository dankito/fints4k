package net.dankito.banking.fints.messages.datenelemente.implementierte.account

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.JaNein


/**
 * Mit dieser Option kann gewählt werden, ob die angeforderten Informationen (z.B. Salden, Umsätze)
 * nur zu dem angegebenen oder zu allen Anlagekonten des Kunden, für die er eine
 * Zugriffsberechtigung besitzt, zurückgemeldet werden sollen.
 */
open class AlleKonten(allAccounts: Boolean, existenzstatus: Existenzstatus) : JaNein(allAccounts, existenzstatus)
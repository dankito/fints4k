package net.dankito.fints.messages.datenelemente.implementierte.account

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Maximale Anzahl rückzumeldender Einträge bei Abholaufträgen, Kreditinstitutsangeboten
 * oder –informationen (vgl. [Formals], Kap. B.6.3).
 */
open class MaximaleAnzahlEintraege(maxAmount: Int, existenzstatus: Existenzstatus) : NumerischesDatenelement(maxAmount, 4, existenzstatus)
package net.dankito.banking.fints.messages.datenelemente

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.Nachrichtenteil


abstract class DatenelementBase(val existenzstatus: Existenzstatus) : Nachrichtenteil()
package net.dankito.fints.messages.datenelemente

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.Nachrichtenteil


abstract class DatenelementBase(val existenzstatus: Existenzstatus) : Nachrichtenteil()
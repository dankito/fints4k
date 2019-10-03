package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Identifikation


class DialogId(dialogId: String) : Identifikation(dialogId, Existenzstatus.Mandatory)
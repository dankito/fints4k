package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Information darüber, wie die Beantwortung des Kundenauftrags an einem bestimmten Punkt kontrolliert
 * beendet und aufgesetzt werden kann, falls die Rückmeldung des Kreditinstituts nicht in einem
 * einzigen Auftragssegment erfolgen kann (s. [Formals]).
 */
open class Aufsetzpunkt(continuationId: String, existenzstatus: Existenzstatus)
    : AlphanumerischesDatenelement(continuationId, existenzstatus, 35)
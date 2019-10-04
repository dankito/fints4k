package net.dankito.fints.messages.datenelemente.implementierte.tan

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.BinaerDatenelement


/**
 * Bei Verwendung von Zwei-Schritt-Verfahren mit unidirektionaler Kopplung (vgl. hierzu [HHD_UC])
 * müssen zusätzlich zum Datenelement „Challenge“ die Daten für die Übertragung z. B. über eine
 * optische Schnittstelle bereitgestellt werden. Die einzelnen Datenelemente der „Challenge HHD_UC“
 * sind in [HHD_UC] beschrieben und werden hier im FinTS Data Dictionary nicht näher erläutert.
 * Da HHD_UC einen anderen Basiszeichensatz verwendet (ISO 646) wird die HHD_UC-Struktur als binär
 * definiert. Als maximale Länge kann ein Wert von 128 angenommen werden.
 */
open class ChallengeHHD_UC(challenge: ByteArray, existenzstatus: Existenzstatus)
    : BinaerDatenelement(challenge, existenzstatus, 128)
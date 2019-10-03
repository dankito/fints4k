package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Version der HBCI-/FinTS-Schnittstellenspezifikation, die der jeweiligen Realisierung zugrunde liegt.
 *
 * HBCI- bzw. FinTS-Versionen, die vor Version 2.0.1 veröffentlicht wurden, werden
 * kreditinstitutsseitig nicht unterstützt.
 *
 * Ein geregelter Dialog ist nur zwischen Systemen möglich, die mit derselben HBCI-/FinTS-Version
 * arbeiten. Stimmt die vom Kunden übermittelte HBCI-/FinTS-Version nicht mit einer der vom
 * Kreditinstitut in den BPD mitgeteilten unterstützten HBCI-/FinTS-Versionen überein, so muss der
 * Dialog vom Kreditinstitut beendet werden. Innerhalb eines Dialoges dürfen nicht Nachrichten
 * unterschiedlicher HBCI-/FinTS-Versionen gesendet werden.
 *
 * Segment- und HBCI-/FinTS-Versionen werden unabhängig voneinander geführt. Innerhalb eines HBCI-/
 * FinTS-Dialoges dürfen nur Versionen administrativer Segmente gesendet werden, die der angegebenen
 * HBCI-/FinTS-Version entsprechen. Im Rahmen einer HBCI-/FinTS-Version wird eine Liste der zugehörigen
 * Segmentversionen veröffentlicht (s. [Messages], Anlagen). Weiterhin werden in dieser Liste auch die
 * zusätzlich noch unterstützten Segmentversionen genannt.
 *
 * Der Zeitpunkt der Unterstützung einer neuen HBCI-/FinTS-Version kann zwischen den Kreditinstituten variieren.
 *
 * Zulässige Werte:
 *
 * - Version 2.0.1: 201 (Spezifikationsstatus: obsolet)
 * - Version 2.1:  210 (Spezifikationsstatus: obsolet)
 * - Version 2.2:  220 (Spezifikationsstatus: obsolet)
 * - Version 3.0:  300
 */
class HbciVersionDatenelement(version: HbciVersion) : NumerischesDatenelement(version.versionNumber, 3, Existenzstatus.Mandatory)
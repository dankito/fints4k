package net.dankito.fints.messages.nachrichten.implementierte

import net.dankito.fints.messages.datenelemente.implementierte.*


/**
 * Um Kunden die Möglichkeit zu geben, sich anonym anzumelden, um sich bspw. über die
 * angebotenen Geschäftsvorfälle fremder Kreditinstitute (von denen sie keine BPD besitzen)
 * zu informieren bzw. nicht-signierungspflichtige Aufträge bei fremden Kreditinstituten
 * einreichen zu können, kann sich der Kunde anonym (als Gast) anmelden.
 *
 * Bei anonymen Dialogen werden Nachrichten weder signiert, noch können sie verschlüsselt und komprimiert werden.
 */
open class DialoginitialisierungAnonym(
    bankCountryCode: Int,
    bankCode: String,
    productName: String,
    productVersion: String
) : Dialoginitialisierung(125, bankCountryCode, bankCode, KundenID.Anonymous, KundensystemID.Anonymous,
    BPDVersion.VersionNotReceivedYet, UPDVersion.VersionNotReceivedYet, Dialogsprache.Default, productName, productVersion)
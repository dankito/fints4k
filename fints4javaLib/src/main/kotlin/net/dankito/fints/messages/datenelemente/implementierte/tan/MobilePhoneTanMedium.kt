package net.dankito.fints.messages.datenelemente.implementierte.tan

import net.dankito.fints.messages.datenelementgruppen.implementierte.account.KontoverbindungInternational


open class MobilePhoneTanMedium(
    mediumClass: TanMediumKlasse,
    status: TanMediumStatus,
    val mediumName: String,
    val concealedPhoneNumber: String?,
    val phoneNumber: String?,
    val smsDebitAccount: KontoverbindungInternational? = null
) : TanMedium(mediumClass, status) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MobilePhoneTanMedium) return false
        if (!super.equals(other)) return false

        if (mediumName != other.mediumName) return false
        if (concealedPhoneNumber != other.concealedPhoneNumber) return false
        if (phoneNumber != other.phoneNumber) return false
        if (smsDebitAccount != other.smsDebitAccount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (mediumName.hashCode())
        result = 31 * result + (concealedPhoneNumber?.hashCode() ?: 0)
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (smsDebitAccount?.hashCode() ?: 0)
        return result
    }


    override fun toString(): String {
        return super.toString() + " $mediumName ${phoneNumber ?: concealedPhoneNumber ?: ""}"
    }

}
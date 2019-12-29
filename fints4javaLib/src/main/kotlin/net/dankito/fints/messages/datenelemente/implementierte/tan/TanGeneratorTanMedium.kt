package net.dankito.fints.messages.datenelemente.implementierte.tan

import java.util.*


class TanGeneratorTanMedium(
    mediumClass: TanMediumKlasse,
    status: TanMediumStatus,
    val cardNumber: String,
    val followUpCardNumber: String?,
    val cardType: String?,
    val validFrom: Date?,
    val validTo: Date?,
    val mediaName: String?
) : TanMedium(mediumClass, status) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as TanGeneratorTanMedium

        if (cardNumber != other.cardNumber) return false
        if (followUpCardNumber != other.followUpCardNumber) return false
        if (cardType != other.cardType) return false
        if (validFrom != other.validFrom) return false
        if (validTo != other.validTo) return false
        if (mediaName != other.mediaName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + cardNumber.hashCode()
        result = 31 * result + followUpCardNumber.hashCode()
        result = 31 * result + (cardType?.hashCode() ?: 0)
        result = 31 * result + (validFrom?.hashCode() ?: 0)
        result = 31 * result + (validTo?.hashCode() ?: 0)
        result = 31 * result + (mediaName?.hashCode() ?: 0)
        return result
    }


    override fun toString(): String {
        return super.toString() + " $mediaName $cardNumber (follow up: ${followUpCardNumber ?: "-"})"
    }

}
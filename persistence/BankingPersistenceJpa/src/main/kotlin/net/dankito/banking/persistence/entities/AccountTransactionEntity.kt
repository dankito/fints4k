package net.dankito.banking.persistence.entities

import java.math.BigDecimal
import java.util.*
import javax.persistence.*


@Entity(name = "account_transaction")
open class AccountTransactionEntity(

    @Column(name = "amount", nullable = false)
    val amount: BigDecimal,

    @Column(name = "booking_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    val bookingDate: Date,

    @Column(name = "usage", nullable = false)
    val usage: String,

    @Column(name = "other_party_name")
    val otherPartyName: String?,

    @Column(name = "other_party_bank_code")
    val otherPartyBankCode: String?,

    @Column(name = "other_party_account_id")
    val otherPartyAccountId: String?,

    @Column(name = "booking_text")
    val bookingText: String?,

    @Column(name = "balance")
    val balance: BigDecimal?,

    @Column(name = "currency", nullable = false)
    val currency: String,

    @ManyToOne
    val bankAccount: BankAccountEntity

) : BaseEntity() {


    internal constructor() : this(BigDecimal.ZERO, Date(), "", "", "", "", "", BigDecimal.ZERO, "", BankAccountEntity()) // for object deserializers



    override fun toString(): String {
        return "$amount $otherPartyName: $usage"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountTransactionEntity) return false

        if (bookingDate != other.bookingDate) return false
        if (usage != other.usage) return false
        if (amount.compareTo(other.amount) != 0) return false
        if (otherPartyName != other.otherPartyName) return false
        if (otherPartyAccountId != other.otherPartyAccountId) return false
        if (otherPartyBankCode != other.otherPartyBankCode) return false

        return true
    }

    override fun hashCode(): Int {
        // e.g. BigDecimal of "150.0" produces another hash code as BigDecimal of "-1.5E+2" even though their values are the same -> has to be converted to double before
        var result = amount.toDouble().hashCode()
        result = 31 * result + usage.hashCode()
        result = 31 * result + otherPartyName.hashCode()
        result = 31 * result + otherPartyAccountId.hashCode()
        result = 31 * result + otherPartyBankCode.hashCode()
        result = 31 * result + bookingDate.hashCode()
        return result
    }

}
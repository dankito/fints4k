package net.dankito.banking.persistence.entities

import net.dankito.banking.ui.model.BankAccountType
import java.math.BigDecimal
import javax.persistence.*


@Entity(name = "bank_accounts")
open class BankAccountEntity @JvmOverloads constructor(

    @ManyToOne
    val account: AccountEntity,

    @Column(name = "identifier", nullable = false)
    var identifier: String,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "iban")
    var iban: String?,

    @Column(name = "sub_account_number")
    var subAccountNumber: String?,

    @Column(name = "balance", nullable = false)
    var balance: BigDecimal = BigDecimal.ZERO,

    @Column(name = "currency", nullable = false)
    var currency: String,

    @Column(name = "type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    var type: BankAccountType = BankAccountType.Girokonto,

    @OneToMany(mappedBy = "bankAccount", cascade = [ CascadeType.ALL ], orphanRemoval = true)
    @JoinColumn(name = "transactions")
    var transactions: List<AccountTransactionEntity> = mutableListOf()

) : BaseEntity() {


    internal constructor() : this(AccountEntity(), "", "", "", null, BigDecimal.ZERO, "") // for object deserializers


    override fun toString(): String {
        return "$name ($identifier)"
    }

}
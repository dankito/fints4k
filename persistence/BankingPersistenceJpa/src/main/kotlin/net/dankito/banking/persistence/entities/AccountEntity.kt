package net.dankito.banking.persistence.entities

import net.dankito.banking.ui.model.tan.TanMedium
import net.dankito.banking.ui.model.tan.TanProcedure
import javax.persistence.*


@Entity(name = "accounts")
open class AccountEntity @JvmOverloads constructor(

    @OneToOne(cascade = [ CascadeType.ALL ], orphanRemoval = true)
    val bank: BankEntity,

    @Column(name = "customer_id", nullable = false)
    var customerId: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "user_id", nullable = false)
    var userId: String,

    @OneToMany(mappedBy = "account", cascade = [ CascadeType.ALL ], orphanRemoval = true)
    @JoinColumn(name = "bank_accounts")
    var bankAccounts: List<BankAccountEntity> = mutableListOf()

) : BaseEntity() {

    // TODO:

    var supportedTanProcedures: List<TanProcedure> = listOf()

    var selectedTanProcedure: TanProcedure? = null

    var tanMedia: List<TanMedium> = listOf()


    internal constructor() : this(BankEntity(), "", "", "", "") // for object deserializers


    override fun toString(): String {
        return "$customerId $name"
    }

}
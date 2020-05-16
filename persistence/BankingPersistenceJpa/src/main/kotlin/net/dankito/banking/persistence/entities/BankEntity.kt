package net.dankito.banking.persistence.entities

import javax.persistence.Column
import javax.persistence.Entity


@Entity(name = "banks")
open class BankEntity(

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "bank_code", nullable = false)
    val bankCode: String,

    @Column(name = "bic", nullable = false)
    var bic: String,

    @Column(name = "fin_ts_server_address", nullable = false)
    var finTsServerAddress: String,

    @Column(name = "icon_url", nullable = false)
    var iconUrl: String? = null

) : BaseEntity() {

    internal constructor() : this("", "", "", "")


    override fun toString(): String {
        return "$bankCode $name"
    }

}
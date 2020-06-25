package net.dankito.banking.ui.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import net.dankito.banking.ui.model.tan.TanMedium
import net.dankito.banking.ui.model.tan.TanMediumStatus
import net.dankito.banking.ui.model.tan.TanProcedure
import java.math.BigDecimal
import java.util.*


@JsonIdentityInfo(property = "id", generator = ObjectIdGenerators.PropertyGenerator::class) // to avoid stack overflow due to circular references
open class Customer(
    open var bankCode: String,
    open var customerId: String,
    open var password: String,
    open var finTsServerAddress: String,
    open var bankName: String,
    open var bic: String,
    open var customerName: String,
    open var userId: String = customerId,
    open var iconUrl: String? = null,
    open var accounts: List<BankAccount> = listOf()
) {


    internal constructor() : this("", "", "", "", "", "", "") // for object deserializers


    open var id: String = UUID.randomUUID().toString()


    open var supportedTanProcedures: List<TanProcedure> = listOf()

    open var selectedTanProcedure: TanProcedure? = null

    open var tanMedia: List<TanMedium> = listOf()

    open val tanMediaSorted: List<TanMedium>
        get() = tanMedia.sortedByDescending { it.status == TanMediumStatus.Used }


    open val displayName: String
        get() = bankName

    open val balance: BigDecimal
        get() = accounts.map { it.balance }.fold(BigDecimal.ZERO) { acc, e -> acc + e }

    open val transactions: List<AccountTransaction>
        get() = accounts.flatMap { it.bookedTransactions }


    override fun toString(): String {
        return "$customerName ($customerId)"
    }

}
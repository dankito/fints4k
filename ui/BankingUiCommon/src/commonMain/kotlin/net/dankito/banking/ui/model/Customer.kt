package net.dankito.banking.ui.model

//import com.fasterxml.jackson.annotation.JsonIdentityInfo
//import com.fasterxml.jackson.annotation.ObjectIdGenerators
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.sum
import net.dankito.banking.ui.model.tan.TanMedium
import net.dankito.banking.ui.model.tan.TanMediumStatus
import net.dankito.banking.ui.model.tan.TanProcedure
import net.dankito.utils.multiplatform.UUID


//@JsonIdentityInfo(property = "technicalId", generator = ObjectIdGenerators.PropertyGenerator::class) // to avoid stack overflow due to circular references
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

    /*      convenience constructors for languages not supporting default values        */

    constructor(bankCode: String, customerId: String, password: String, finTsServerAddress: String)
            : this(bankCode, customerId, password, finTsServerAddress, "", "", "")


    open var technicalId: String = UUID.random()


    open var supportedTanProcedures: List<TanProcedure> = listOf()

    open var selectedTanProcedure: TanProcedure? = null

    open var tanMedia: List<TanMedium> = listOf()

    open val tanMediaSorted: List<TanMedium>
        get() = tanMedia.sortedByDescending { it.status == TanMediumStatus.Used }


    open val displayName: String
        get() = bankName

    open val balance: BigDecimal
        get() = accounts.map { it.balance }.sum()

    open val transactions: List<AccountTransaction>
        get() = accounts.flatMap { it.bookedTransactions }


    override fun toString(): String {
        return "$customerName ($customerId)"
    }

}
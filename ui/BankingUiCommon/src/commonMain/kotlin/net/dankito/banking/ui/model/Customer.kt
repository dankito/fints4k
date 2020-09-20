package net.dankito.banking.ui.model

import net.dankito.banking.ui.model.tan.TanMedium
import net.dankito.banking.ui.model.tan.TanProcedure
import net.dankito.utils.multiplatform.UUID


open class Customer(
    override var bankCode: String,
    override var customerId: String,
    override var password: String,
    override var finTsServerAddress: String,
    override var bankName: String,
    override var bic: String,
    override var customerName: String,
    override var userId: String = customerId,
    override var iconUrl: String? = null,
    override var accounts: List<TypedBankAccount> = listOf()
) : TypedCustomer {


    internal constructor() : this("", "", "", "", "", "", "") // for object deserializers

    /*      convenience constructors for languages not supporting default values        */

    constructor(bankCode: String, customerId: String, password: String, finTsServerAddress: String)
            : this(bankCode, customerId, password, finTsServerAddress, "", "", "")


    override var technicalId: String = UUID.random()


    override var supportedTanProcedures: List<TanProcedure> = listOf()

    override var selectedTanProcedure: TanProcedure? = null

    override var tanMedia: List<TanMedium> = listOf()


    override var countDaysForWhichTransactionsAreKept: Int? = null


    override var userSetDisplayName: String? = null

    override var displayIndex: Int = 0


    override fun toString(): String {
        return stringRepresentation
    }

}
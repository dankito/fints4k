package net.dankito.banking.ui.model

import net.dankito.banking.ui.model.tan.TanMedium
import net.dankito.banking.ui.model.tan.TanMethod
import net.dankito.utils.multiplatform.UUID


open class BankData(
    override var bankCode: String,
    override var userName: String,
    override var password: String,
    override var finTsServerAddress: String,
    override var bankName: String,
    override var bic: String,
    override var customerName: String,
    override var userId: String = userName,
    override var iconUrl: String? = null,
    override var accounts: List<TypedBankAccount> = listOf()
) : TypedBankData {


    internal constructor() : this("", "", "", "", "", "", "") // for object deserializers

    /*      convenience constructors for languages not supporting default values        */

    constructor(bankCode: String, userName: String, password: String, finTsServerAddress: String)
            : this(bankCode, userName, password, finTsServerAddress, "", "", "")


    override var technicalId: String = UUID.random()


    override var supportedTanMethods: List<TanMethod> = listOf()

    override var selectedTanMethod: TanMethod? = null

    override var tanMedia: List<TanMedium> = listOf()


    override var countDaysForWhichTransactionsAreKept: Int? = null


    override var userSetDisplayName: String? = null

    override var displayIndex: Int = 0


    override fun toString(): String {
        return stringRepresentation
    }

}
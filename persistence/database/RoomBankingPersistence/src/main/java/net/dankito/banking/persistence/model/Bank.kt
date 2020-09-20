package net.dankito.banking.persistence.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import net.dankito.banking.persistence.dao.BaseDao
import net.dankito.banking.ui.model.TypedBankAccount
import net.dankito.banking.ui.model.TypedCustomer
import net.dankito.banking.ui.model.tan.TanMedium
import net.dankito.banking.ui.model.tan.TanProcedure


@Entity
open class Bank(
    override var bankCode: String,
    override var customerId: String,
    override var password: String,
    override var finTsServerAddress: String,
    override var bankName: String,
    override var bic: String,
    override var customerName: String,

    override var userId: String = customerId,
    override var iconUrl: String? = null,

    @Ignore
    override var accounts: List<TypedBankAccount> = listOf(),
    
    @Ignore
    override var supportedTanProcedures: List<TanProcedure> = listOf(),
    @Ignore
    override var selectedTanProcedure: TanProcedure? = null,
    @Ignore
    override var tanMedia: List<TanMedium> = listOf(),


    override var countDaysForWhichTransactionsAreKept: Int? = null,


    @PrimaryKey(autoGenerate = true)
    open var id: Long = BaseDao.IdNotSet,

    override var technicalId: String = id.toString(),

    override var userSetDisplayName: String? = null,
    override var displayIndex: Int = 0
) : TypedCustomer {

    internal constructor() : this("", "", "", "", "", "", "") // for object deserializers


    open var selectedTanProcedureId: String? = null

    @Ignore
    open var tanMediumEntities = listOf<net.dankito.banking.persistence.model.TanMedium>()


    override fun toString(): String {
        return stringRepresentation
    }

}
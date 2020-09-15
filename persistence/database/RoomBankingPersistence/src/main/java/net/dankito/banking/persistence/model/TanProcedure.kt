package net.dankito.banking.persistence.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.dankito.banking.persistence.dao.BaseDao
import net.dankito.banking.ui.model.tan.AllowedTanFormat
import net.dankito.banking.ui.model.tan.TanProcedure
import net.dankito.banking.ui.model.tan.TanProcedureType


@Entity
open class TanProcedure(
    displayName: String,
    type: TanProcedureType,
    bankInternalProcedureCode: String,
    maxTanInputLength: Int? = null,
    allowedTanFormat: AllowedTanFormat = AllowedTanFormat.Alphanumeric
) : TanProcedure(displayName, type, bankInternalProcedureCode, maxTanInputLength, allowedTanFormat) {

    internal constructor() : this("", TanProcedureType.EnterTan, "") // for object deserializers


    @PrimaryKey
    open var id: String = technicalId

    // Room doesn't allow me to add getters and setters -> have to map it manually
    open var bankId: Long = BaseDao.ObjectNotInsertedId

}
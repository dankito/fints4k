package net.dankito.banking.persistence.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.dankito.banking.persistence.dao.BaseDao
import net.dankito.banking.ui.model.tan.TanMediumStatus


@Entity
open class TanMedium(
    @PrimaryKey
    open var id: String,
    open var bankId: Long,

    open var type: TanMediumType,
    open var displayName: String,
    open var status: TanMediumStatus,
    open var cardNumber: String? = null,
    open var phoneNumber: String? = null
) {

    internal constructor() : this("", BaseDao.ObjectNotInsertedId, TanMediumType.OtherTanMedium, "", TanMediumStatus.Available) // for object deserializers

}
package net.dankito.banking.persistence.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.dankito.banking.persistence.dao.BaseDao
import net.dankito.banking.ui.model.settings.TanMethodSettings


@Entity
open class TanMethodSettings(
    @PrimaryKey
    open var id: Int,
    width: Int,
    height: Int,
    space: Int = -1,
    frequency: Int = -1
) : TanMethodSettings(width, height, space, frequency) {

    internal constructor() : this(BaseDao.IdNotSet.toInt(), 0, 0) // for object deserializers

}
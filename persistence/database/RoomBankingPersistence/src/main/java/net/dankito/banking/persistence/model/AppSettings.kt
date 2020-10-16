package net.dankito.banking.persistence.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.dankito.banking.persistence.RoomBankingPersistence
import net.dankito.banking.ui.model.settings.AppSettings


@Entity
open class AppSettings(
    open var automaticallyUpdateAccounts: Boolean = true,
    open var automaticallyUpdateAccountsAfterMinutes: Int = AppSettings.DefaultAutomaticallyUpdateAccountsAfterMinutes,
    open var lockAppAfterMinutes: Int? = null
) {

    internal constructor() : this(true, AppSettings.DefaultAutomaticallyUpdateAccountsAfterMinutes, null)

    @PrimaryKey
    open var id: Int = RoomBankingPersistence.AppSettingsId

}
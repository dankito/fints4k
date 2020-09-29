package net.dankito.banking.persistence.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.dankito.banking.persistence.RoomBankingPersistence
import net.dankito.banking.ui.model.settings.AppSettings


@Entity
open class AppSettings(
    open var updateAccountsAutomatically: Boolean = true,
    open var refreshAccountsAfterMinutes: Int = AppSettings.DefaultRefreshAccountsAfterMinutes
) {

    internal constructor() : this(true, AppSettings.DefaultRefreshAccountsAfterMinutes)

    @PrimaryKey
    open var id: Int = RoomBankingPersistence.AppSettingsId

}
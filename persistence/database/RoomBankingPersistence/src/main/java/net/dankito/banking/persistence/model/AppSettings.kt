package net.dankito.banking.persistence.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.dankito.banking.persistence.RoomBankingPersistence
import net.dankito.banking.ui.model.settings.AppSettings


@Entity
open class AppSettings(
    open var automaticallyUpdateAccountsAfterMinutes: Int? = AppSettings.DefaultAutomaticallyUpdateAccountsAfterMinutes,
    open var lockAppAfterMinutes: Int? = null,
    open var screenshotsAllowed: Boolean = false,
    open var lastSelectedExportFolder: String? = null
) {

    internal constructor() : this(AppSettings.DefaultAutomaticallyUpdateAccountsAfterMinutes, null, false)

    @PrimaryKey
    open var id: Int = RoomBankingPersistence.AppSettingsId

}
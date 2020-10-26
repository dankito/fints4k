package net.dankito.banking.ui.model.settings

import net.dankito.utils.multiplatform.UUID


open class AppSettings(
    open var automaticallyUpdateAccountsAfterMinutes: Int? = DefaultAutomaticallyUpdateAccountsAfterMinutes,
    open var lockAppAfterMinutes: Int? = null,
    open var screenshotsAllowed: Boolean = false, // TODO: implement
    open var flickerCodeSettings: TanMethodSettings? = null,
    open var qrCodeSettings: TanMethodSettings? = null,
    open var photoTanSettings: TanMethodSettings? = null,
    open var lastSelectedExportFolder: String? = null // File is not that easily persistable so modeled it as string
) {

    companion object {
        const val DefaultAutomaticallyUpdateAccountsAfterMinutes = 6 * 60 // 6 hours
    }


    internal constructor() : this(DefaultAutomaticallyUpdateAccountsAfterMinutes,  null, false, null, null) // for object deserializers


    open var technicalId: String = UUID.random()

}